"""Train/evaluate temporal autoencoder and export real TFLite model."""

from pathlib import Path
import json
import numpy as np
from sklearn.metrics import precision_recall_fscore_support, confusion_matrix
import tensorflow as tf


SEED = 42
np.random.seed(SEED)
tf.random.set_seed(SEED)


def generate_dataset(samples=9000):
    normal = np.random.normal(loc=22, scale=9, size=(samples, 6)).clip(0, 100)
    malicious = np.random.normal(loc=80, scale=10, size=(samples // 3, 6)).clip(0, 100)
    x = np.vstack([normal, malicious]).astype(np.float32) / 100.0
    y = np.array([0] * len(normal) + [1] * len(malicious))
    idx = np.random.permutation(len(x))
    return x[idx], y[idx]


def make_sequences(x, y, window=8):
    seq_x, seq_y = [], []
    for i in range(len(x) - window + 1):
        seq_x.append(x[i:i + window])
        seq_y.append(int(np.max(y[i:i + window])))
    return np.array(seq_x, dtype=np.float32), np.array(seq_y, dtype=np.int32)


def build_autoencoder(window=8, features=6):
    model = tf.keras.Sequential(
        [
            tf.keras.layers.Input(shape=(window, features)),
            tf.keras.layers.LSTM(32, return_sequences=True),
            tf.keras.layers.LSTM(16, return_sequences=False),
            tf.keras.layers.RepeatVector(window),
            tf.keras.layers.LSTM(16, return_sequences=True),
            tf.keras.layers.LSTM(32, return_sequences=True),
            tf.keras.layers.TimeDistributed(tf.keras.layers.Dense(features)),
        ]
    )
    model.compile(optimizer="adam", loss="mse")
    return model


def evaluate(model, x_test, y_test):
    recon = model.predict(x_test, verbose=0)
    err = np.mean(np.square(x_test - recon), axis=(1, 2))
    threshold = float(np.percentile(err[y_test == 0], 97.5))
    pred = (err > threshold).astype(int)

    precision, recall, f1, _ = precision_recall_fscore_support(
        y_test, pred, average="binary", zero_division=0
    )
    tn, fp, fn, tp = confusion_matrix(y_test, pred).ravel()
    fpr = float(fp / (fp + tn + 1e-9))
    return {
        "threshold": threshold,
        "precision": float(precision),
        "recall": float(recall),
        "f1": float(f1),
        "false_positive_rate": fpr,
        "tp": int(tp),
        "fp": int(fp),
        "tn": int(tn),
        "fn": int(fn),
    }


if __name__ == "__main__":
    out = Path("artifacts")
    out.mkdir(parents=True, exist_ok=True)

    x, y = generate_dataset()
    seq_x, seq_y = make_sequences(x, y, window=8)
    split = int(len(seq_x) * 0.8)
    x_train, y_train = seq_x[:split], seq_y[:split]
    x_test, y_test = seq_x[split:], seq_y[split:]

    # Fit only on benign windows for anomaly training.
    benign_train = x_train[y_train == 0]
    model = build_autoencoder(window=8, features=6)
    model.fit(
        benign_train,
        benign_train,
        validation_split=0.1,
        epochs=12,
        batch_size=64,
        verbose=1,
    )

    metrics = evaluate(model, x_test, y_test)
    (out / "metrics_report.json").write_text(json.dumps(metrics, indent=2))

    model.export(out / "saved_model")
    converter = tf.lite.TFLiteConverter.from_saved_model(str(out / "saved_model"))
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()
    (out / "ransom_model.tflite").write_bytes(tflite_model)

    print("Training complete.")
    print(json.dumps(metrics, indent=2))
