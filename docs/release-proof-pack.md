# Release & Proof Pack Checklist

## Signed Production Artifacts
- Generate upload keystore (`keytool`) and store outside repository.
- Configure `signingConfigs` for release in `app/build.gradle.kts`.
- Build:
  - `./gradlew assembleRelease`
  - `./gradlew bundleRelease`
- Verify signature with `apksigner verify --verbose`.

## Performance Benchmark Evidence
- Capture 15-minute benchmark on at least 2 devices:
  - CPU overhead (% app process)
  - RAM footprint (MB)
  - Battery drain estimate (mAh/h)
- Export screenshots from Runtime Footprint card while idle and under simulation.

## Detection Quality Evidence
- Train model via `ml/train_model.py`.
- Include `ml/artifacts/metrics_report.json` in submission appendix.
- Report:
  - Precision
  - Recall
  - F1 score
  - False-positive rate

## Play Integrity Compliance Prep
- Create app in Google Play Console.
- Enable Play Integrity API and connect project credentials.
- Add backend verification endpoint for integrity tokens.
- Define action policy:
  - `MEETS_DEVICE_INTEGRITY` only: full features
  - otherwise: limited mode + warning

## Cloud/Multi-Device Proof
- Add Firebase config (`google-services.json`) to app module.
- Enable Firestore and verify `threat_events` writes.
- Create simple dashboard query grouped by user/device.

## Security Hardening Proof
- Demonstrate encrypted event logs at rest.
- Show anti-tamper signals in runtime card.
- Document non-root preventive control boundaries and mitigations.
