# Telemetry Data for Training & Validation

Place real telemetry files here for model training and validation.

## Supported Formats

- **JSON** – RansomShield app export format
- **CSV** – Header: `timestampMs,cpuUsage,memoryUsage,ioOpsPerSec,fileMutationBurst,accessibilityAbuseSignal,suspiciousPermissionScore,label`

## Label

- `0` = benign (normal app behavior)
- `1` = malicious (ransomware or suspicious behavior)

## How to Add Data

1. **From the app**: Export telemetry (JSON or CSV) and copy files here.
2. **From datasets**: Convert AndroZoo, CICAndMal2017, etc. to this format. See `docs/validation.md`.

## Example Layout

```
data/
├── benign_export_20250322.json
├── malicious_sim_20250322.json
├── benign_from_dataset.csv
└── malicious_from_androzoo.csv
```
