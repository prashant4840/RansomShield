# RansomShield Validation Guide

This guide explains how to validate RansomShield using **real telemetry** and **ransomware datasets** for production readiness.

---

## Overview

| Component | Purpose |
|-----------|---------|
| **Telemetry Export** (Android app) | Capture real device behavior → JSON/CSV |
| **load_telemetry.py** | Load exported or dataset telemetry |
| **train_model.py** | Train on real or synthetic data |
| **validate_model.py** | Benchmark model against labeled data |

---

## 1. Capturing Real Telemetry from the App

### Steps

1. **Benign telemetry** (normal use):
   - Enable monitoring (simulation OFF)
   - Use the device normally for 5–15 minutes
   - Export JSON or CSV (label = benign)

2. **Malicious-like telemetry** (simulated):
   - Enable monitoring + Threat Simulation Mode
   - Let it run for several minutes
   - Export JSON or CSV (label = malicious)

3. **Transfer files**:
   - Share exported files to your computer (e.g., Drive, email, USB)
   - Place in `ml/data/` (e.g., `benign_export.json`, `malicious_sim.json`)

### Export Format

- **JSON**: RansomShield schema with `samples` array and `metadata`
- **CSV**: `timestampMs,cpuUsage,memoryUsage,ioOpsPerSec,fileMutationBurst,accessibilityAbuseSignal,suspiciousPermissionScore,label`

---

## 2. Ransomware Dataset Sources

For production validation, use public Android malware/ransomware datasets:

| Source | Description | Access |
|--------|-------------|--------|
| **AndroZoo** | Large Android app corpus (benign + malicious) | [androzoo.uni.lu](https://androzoo.uni.lu) — requires API key |
| **VirusTotal** | Malware samples + reports | API (requires key) or manual download |
| **CICAndMal2017** | Canadian Institute for Cybersecurity — Android malware | [dataset](https://www.unb.ca/cic/datasets/andmal2017.html) |
| **DREBIN** | Android malware dataset | Academic/research use |
| **AMD** (Android Malware Dataset) | Labeled Android malware | [dataset](https://www.kaggle.com/datasets) |

### Important Notes

- **Do not** execute real malware on a primary device. Use an isolated emulator or sandbox.
- Many datasets provide APKs; you need to **run** them and **capture telemetry** via RansomShield’s collectors or instrumented hooks.
- For datasets that provide **behavior traces** (API calls, syscalls), you may need to map those to RansomShield’s 6 features (CPU, memory, I/O, file mutation, accessibility, permissions).

### Mapping External Data to RansomShield Features

If your dataset has different columns, create a conversion script that maps to:

| RansomShield Feature | Typical Source |
|----------------------|----------------|
| `cpuUsage` | Process CPU % or similar |
| `memoryUsage` | Process memory % |
| `ioOpsPerSec` | I/O or disk operations rate |
| `fileMutationBurst` | File create/write/encrypt rate |
| `accessibilityAbuseSignal` | Accessibility service usage, overlay events |
| `suspiciousPermissionScore` | Permission count or risk score |

Values should be in 0–100 range (or will be normalized).

---

## 3. Training on Real Data

```bash
cd ml
source .venv/bin/activate  # or .venv\Scripts\activate on Windows

# From a directory with benign + malicious JSON/CSV
python train_model.py --data data/

# From specific files
python train_model.py --data data/benign.json data/malicious.json

# With more epochs for larger datasets
python train_model.py --data data/ --epochs 25
```

Ensure you have **both benign and malicious** samples. The model trains on benign-only (anomaly detection) but evaluation uses labeled data.

---

## 4. Validating the Model

```bash
# Validate TFLite model against your data
python validate_model.py --data ml/data/ --model artifacts/ransom_model.tflite

# Multiple paths
python validate_model.py --data data/benign/ data/malicious/ --output artifacts/validation_report.json
```

Output (`artifacts/validation_report.json`):

```json
{
  "data": { "total": 1000, "benign": 700, "malicious": 300 },
  "tflite": {
    "precision": 0.92,
    "recall": 0.88,
    "f1": 0.90,
    "false_positive_rate": 0.05,
    "roc_auc": 0.94
  }
}
```

---

## 5. Recommended Validation Workflow

1. **Baseline (synthetic)**  
   Run `train_model.py` and `validate_model.py` with synthetic data to ensure the pipeline works.

2. **Real telemetry (app export)**  
   Export benign + simulation telemetry from the app, train and validate.

3. **External dataset**  
   Obtain ransomware/malware dataset, convert to RansomShield format, train and validate.

4. **Threshold tuning**  
   Use `validate_model.py` output to adjust the 97.5th percentile threshold if FPR is too high.

---

## 6. File Structure

```
ml/
├── data/                    # Put exported/dataset telemetry here
│   ├── benign.json
│   ├── malicious.json
│   └── README.md
├── artifacts/
│   ├── ransom_model.tflite
│   ├── metrics_report.json
│   └── validation_report.json
├── load_telemetry.py
├── train_model.py
├── validate_model.py
└── requirements.txt
```

---

## 7. Reducing False Positives

If validation shows high false positive rate:

- Use `--fpr-percentile 98.5` or `99` when training and validating (stricter threshold)
- Add more diverse benign telemetry to `data/`
- See `docs/REDUCING_FALSE_POSITIVES.md` for full tuning guide

---

## 8. Metrics to Track

| Metric | Target | Notes |
|--------|--------|-------|
| **Precision** | > 0.85 | Few false alarms |
| **Recall** | > 0.80 | Catch most threats |
| **F1** | > 0.82 | Balance of both |
| **False Positive Rate** | < 0.10 | User trust |
| **ROC-AUC** | > 0.90 | Discrimination quality |
