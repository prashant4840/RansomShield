# Reducing False Positives

RansomShield uses several strategies to keep false positives low. You can tune them further.

---

## 1. In-App Logic (Already Implemented)

The `ThreatInferenceEngine` applies:

| Strategy | What it does |
|----------|--------------|
| **Sustained alert** | Requires 3 consecutive high readings before escalating to HIGH (avoids one-off spikes from updates, syncs) |
| **Multi-signal confirmation** | HIGH only when 2+ strong signals agree (file mutation + accessibility, I/O + CPU/memory, etc.) |
| **Hysteresis** | When already HIGH, requires 3 consecutive low readings before downgrading to LOW |
| **Raised thresholds** | HIGH bar raised to 85 (from 80); MEDIUM starts at 55 |

---

## 2. ML Training — Tune `--fpr-percentile`

The model uses the **97.5th percentile** of benign reconstruction error as the anomaly threshold. Raise it to reduce false positives (at the cost of more missed threats).

```bash
# Default (97.5th percentile)
python train_model.py --data data/

# Fewer false positives — stricter threshold
python train_model.py --data data/ --fpr-percentile 98.5

# Even stricter
python train_model.py --data data/ --fpr-percentile 99
```

| Percentile | Effect |
|------------|--------|
| 97.5 (default) | Balanced |
| 98–98.5 | Fewer FPs, slightly lower recall |
| 99–99.5 | Very few FPs, more missed threats |

---

## 3. Validation — Find the Right Percentile

Run validation at different percentiles to pick one that meets your FPR target:

```bash
# Compare FPR at different percentiles
python validate_model.py --data data/ --fpr-percentile 97.5
python validate_model.py --data data/ --fpr-percentile 98.5
python validate_model.py --data data/ --fpr-percentile 99
```

Check `artifacts/validation_report.json` for `false_positive_rate`, `precision`, `recall`.

---

## 4. Use Real Benign Data

The biggest improvement comes from training and validating on real device telemetry:

1. Export benign telemetry from the app (monitoring ON, simulation OFF) during normal use.
2. Use a mix of apps: browsing, social, productivity, games.
3. Train: `python train_model.py --data data/`
4. Validate: `python validate_model.py --data data/`

More diverse benign data helps the model learn what “normal” looks like.

---

## 5. Tuning the App Constants (Advanced)

To adjust in-app logic, edit `ThreatInferenceEngine.kt`:

```kotlin
companion object {
    private const val SUSTAINED_HIGH_REQUIRED = 3   // Increase to 4–5 for stricter HIGH
    private const val SUSTAINED_LOW_TO_DOWNGRADE = 3
    private const val HIGH_RAW_THRESHOLD = 85f      // Increase to 88–90 for stricter HIGH
    private const val MEDIUM_THRESHOLD = 55f
}
```

---

## 6. Quick Reference

| If you see... | Try... |
|---------------|--------|
| Too many false alarms | `--fpr-percentile 98.5` or 99 |
| Missing real threats | Lower percentile (97) or add more malicious training data |
| Oscillating High↔Low | Increase `SUSTAINED_HIGH_REQUIRED` and `SUSTAINED_LOW_TO_DOWNGRADE` |
| Single spike triggers HIGH | Ensure multi-signal logic is on (it is by default) |
