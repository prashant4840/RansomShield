"""
Load telemetry data from JSON or CSV for training and validation.

Supports:
- RansomShield app export format (JSON)
- Generic CSV with columns: timestampMs, cpuUsage, memoryUsage, ioOpsPerSec,
  fileMutationBurst, accessibilityAbuseSignal, suspiciousPermissionScore, label

Label: 0 = benign, 1 = malicious
"""

from pathlib import Path
from typing import Tuple

import numpy as np


FEATURE_NAMES = [
    "cpuUsage", "memoryUsage", "ioOpsPerSec",
    "fileMutationBurst", "accessibilityAbuseSignal", "suspiciousPermissionScore"
]


def load_json(path: Path) -> Tuple[np.ndarray, np.ndarray]:
    """Load from RansomShield JSON export."""
    import json
    with open(path) as f:
        data = json.load(f)
    samples = data.get("samples", data) if isinstance(data, dict) else data
    if isinstance(samples, dict):
        samples = data["samples"]
    rows = []
    labels = []
    for s in samples:
        row = [s["cpuUsage"], s["memoryUsage"], s["ioOpsPerSec"],
               s["fileMutationBurst"], s["accessibilityAbuseSignal"],
               s["suspiciousPermissionScore"]]
        rows.append(row)
        labels.append(int(s.get("label", 0)))
    x = np.array(rows, dtype=np.float32)
    y = np.array(labels, dtype=np.int32)
    return x, y


def load_csv(path: Path) -> Tuple[np.ndarray, np.ndarray]:
    """Load from CSV with header."""
    data = np.genfromtxt(path, delimiter=",", skip_header=1, dtype=np.float32)
    if data.ndim == 1:
        data = data.reshape(1, -1)
    x = data[:, 1:7]  # Skip timestampMs, take 6 features, assume last col is label
    y = data[:, -1].astype(np.int32)
    return x, y


def load_directory(dir_path: Path) -> Tuple[np.ndarray, np.ndarray]:
    """Load all JSON/CSV files from a directory. Merges benign (label 0) and malicious (label 1)."""
    all_x, all_y = [], []
    for f in sorted(dir_path.iterdir()):
        if f.suffix == ".json":
            x, y = load_json(f)
        elif f.suffix == ".csv":
            x, y = load_csv(f)
        else:
            continue
        all_x.append(x)
        all_y.append(y)
    if not all_x:
        raise FileNotFoundError(f"No JSON/CSV files in {dir_path}")
    return np.vstack(all_x), np.concatenate(all_y)


def load_telemetry(path) -> Tuple[np.ndarray, np.ndarray]:
    """Load from file or directory. Auto-detects format."""
    path = Path(path)
    if path.is_dir():
        return load_directory(path)
    if path.suffix == ".json":
        return load_json(path)
    if path.suffix == ".csv":
        return load_csv(path)
    raise ValueError(f"Unsupported format: {path.suffix}")


def load_telemetry_multi(paths) -> Tuple[np.ndarray, np.ndarray]:
    """Load and merge from multiple paths (files or directories)."""
    all_x, all_y = [], []
    for p in paths:
        p = Path(p)
        if p.is_dir():
            for f in sorted(p.iterdir()):
                if f.suffix in (".json", ".csv"):
                    x, y = load_telemetry(f)
                    all_x.append(x)
                    all_y.append(y)
        else:
            x, y = load_telemetry(p)
            all_x.append(x)
            all_y.append(y)
    if not all_x:
        raise FileNotFoundError(f"No data in {paths}")
    return np.vstack(all_x), np.concatenate(all_y)


def make_sequences(x: np.ndarray, y: np.ndarray, window: int = 8) -> Tuple[np.ndarray, np.ndarray]:
    """Convert point-wise data to windowed sequences. Label = 1 if any window step is malicious."""
    seq_x, seq_y = [], []
    for i in range(len(x) - window + 1):
        seq_x.append(x[i:i + window])
        seq_y.append(int(np.max(y[i:i + window])))
    return np.array(seq_x, dtype=np.float32), np.array(seq_y, dtype=np.int32)
