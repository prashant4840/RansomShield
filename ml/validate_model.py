"""
Validate the RansomShield model against labeled telemetry.

Usage:
  python validate_model.py --data ml/data/real_telemetry/   # directory with JSON/CSV
  python validate_model.py --data ml/data/benign.json ml/data/malicious.json  # separate files
  python validate_model.py --model ml/artifacts/ransom_model.tflite --data ml/data/

Outputs: ml/artifacts/validation_report.json with precision, recall, F1, FPR, ROC-AUC (if possible).
"""

import argparse
import json
import sys
from pathlib import Path

import numpy as np
from sklearn.metrics import (
    precision_recall_fscore_support,
    confusion_matrix,
    roc_auc_score,
    average_precision_score,
)

# Add ml to path for imports
sys.path.insert(0, str(Path(__file__).parent))

try:
    import tensorflow as tf
except ImportError:
    tf = None


def load_and_prepare(data_paths, window: int = 8):
    """Load telemetry from paths and create sequences."""
    from load_telemetry import load_telemetry_multi, make_sequences

    x, y = load_telemetry_multi(data_paths)
    x = np.asarray(x, dtype=np.float32) / 100.0
    seq_x, seq_y = make_sequences(x, y, window)
    return seq_x, seq_y


def evaluate_tflite(model_path: Path, x_test: np.ndarray, y_test: np.ndarray, fpr_percentile: float = 97.5):
    """Run TFLite model and compute reconstruction error as anomaly score."""
    interpreter = tf.lite.Interpreter(model_path=str(model_path))
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    errors = []
    for i in range(len(x_test)):
        inp = x_test[i:i + 1].astype(np.float32)
        interpreter.set_tensor(input_details[0]["index"], inp)
        interpreter.invoke()
        out = interpreter.get_tensor(output_details[0]["index"])
        err = np.mean(np.square(inp - out))
        errors.append(float(err))

    errors = np.array(errors)
    benign_err = errors[y_test == 0]
    threshold = float(np.percentile(benign_err, fpr_percentile)) if len(benign_err) > 0 else float(np.median(errors))
    pred = (errors > threshold).astype(int)
    return pred, errors, threshold


def evaluate_keras(saved_model_path: Path, x_test: np.ndarray, y_test: np.ndarray, fpr_percentile: float = 97.5):
    """Run Keras SavedModel and compute reconstruction error."""
    model = tf.keras.models.load_model(saved_model_path)
    recon = model.predict(x_test, verbose=0)
    errors = np.mean(np.square(x_test - recon), axis=(1, 2))
    benign_err = errors[y_test == 0]
    threshold = float(np.percentile(benign_err, fpr_percentile)) if len(benign_err) > 0 else float(np.median(errors))
    pred = (errors > threshold).astype(int)
    return pred, errors, threshold


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--data", nargs="+", required=True, help="Path(s) to telemetry JSON/CSV or directory")
    parser.add_argument("--model", default="artifacts/ransom_model.tflite", help="TFLite model path")
    parser.add_argument("--saved-model", help="Keras SavedModel path (optional, for comparison)")
    parser.add_argument("--window", type=int, default=8)
    parser.add_argument("--output", default="artifacts/validation_report.json")
    parser.add_argument("--fpr-percentile", type=float, default=97.5,
                        help="Threshold percentile. Higher = fewer FPs, lower recall. Try 98-99.5.")
    args = parser.parse_args()

    if tf is None:
        print("TensorFlow required for validation. pip install tensorflow")
        sys.exit(1)

    data_paths = [Path(p) for p in args.data]
    out_path = Path(args.output)
    out_path.parent.mkdir(parents=True, exist_ok=True)

    print("Loading telemetry...")
    x, y = load_and_prepare(data_paths, window=args.window)
    n_benign = int(np.sum(y == 0))
    n_malicious = int(np.sum(y == 1))
    print(f"Loaded {len(x)} sequences: {n_benign} benign, {n_malicious} malicious")

    split = int(len(x) * 0.8)
    x_train, x_test = x[:split], x[split:]
    y_train, y_test = y[:split], y[split:]

    report = {"data": {"total": len(x), "benign": n_benign, "malicious": n_malicious}}

    model_path = Path(args.model)
    if model_path.exists():
        print(f"Validating TFLite model: {model_path}")
        pred, errors, threshold = evaluate_tflite(model_path, x_test, y_test, args.fpr_percentile)
        precision, recall, f1, _ = precision_recall_fscore_support(y_test, pred, average="binary", zero_division=0)
        try:
            tn, fp, fn, tp = confusion_matrix(y_test, pred).ravel()
        except ValueError:
            tn = fp = fn = tp = 0
        fpr = float(fp / (fp + tn + 1e-9))
        tflite_metrics = {
            "threshold": threshold,
            "precision": float(precision),
            "recall": float(recall),
            "f1": float(f1),
            "false_positive_rate": fpr,
            "tp": int(tp), "fp": int(fp), "tn": int(tn), "fn": int(fn),
        }
        try:
            tflite_metrics["roc_auc"] = float(roc_auc_score(y_test, errors))
            tflite_metrics["average_precision"] = float(average_precision_score(y_test, errors))
        except Exception:
            pass
        report["tflite"] = tflite_metrics
        print(json.dumps(tflite_metrics, indent=2))

    saved_model = Path(args.saved_model) if args.saved_model else Path("artifacts/saved_model")
    if saved_model.exists():
        print(f"Validating Keras model: {saved_model}")
        pred, errors, threshold = evaluate_keras(saved_model, x_test, y_test, args.fpr_percentile)
        precision, recall, f1, _ = precision_recall_fscore_support(y_test, pred, average="binary", zero_division=0)
        try:
            tn, fp, fn, tp = confusion_matrix(y_test, pred).ravel()
        except ValueError:
            tn = fp = fn = tp = 0
        fpr = float(fp / (fp + tn + 1e-9))
        keras_metrics = {
            "threshold": threshold,
            "precision": float(precision),
            "recall": float(recall),
            "f1": float(f1),
            "false_positive_rate": fpr,
            "tp": int(tp), "fp": int(fp), "tn": int(tn), "fn": int(fn),
        }
        try:
            keras_metrics["roc_auc"] = float(roc_auc_score(y_test, errors))
            keras_metrics["average_precision"] = float(average_precision_score(y_test, errors))
        except Exception:
            pass
        report["keras"] = keras_metrics

    out_path.write_text(json.dumps(report, indent=2))
    print(f"\nValidation report saved to {out_path}")


if __name__ == "__main__":
    main()
