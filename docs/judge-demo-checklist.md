# Judge Demo Checklist (Tailored to Current Build)

## Build Artifacts (Verified)
- Debug APK: `android-app/app/build/outputs/apk/debug/app-debug.apk`
- Release APK (unsigned): `android-app/app/build/outputs/apk/release/app-release-unsigned.apk`

## Pre-Demo Setup (2 minutes)
- Install debug APK on demo phone/emulator for quick retries.
- Keep release APK ready as production artifact proof.
- Ensure app launch screen shows `RansomShield SOC`.
- Keep device brightness high and dark mode enabled for cyber UI impact.

## Live Demo Flow (3-4 minutes)
1. **Baseline**
   - Open app and show low/normal behavior state.
   - Point out SOC design, threat timeline, and security score.
2. **Enable Monitoring**
   - Turn on `Live Monitoring`.
   - Explain real-time behavior signals (CPU, memory, IO, file mutation, accessibility, permissions).
3. **Trigger Threat Simulation**
   - Turn on `Threat Simulation Mode`.
   - Show risk score rising and timeline spikes.
4. **Explainability Moment**
   - Read `Explainable Alert` reasons.
   - Mention behavior-based detection (not signature-based).
5. **One-Tap Response**
   - Tap `Kill`, `Lock`, `Rollback`, `Safe Mode`.
   - Explain this is safe simulation of preventive actions.
6. **AI Assistant + Ranking**
   - Show AI guidance text and app risk ranking list.
   - Mention cloud-sync hook and TFLite-ready runtime design.

## Judge Talking Points (High Impact)
- Detects ransomware **before encryption/lockscreen hijack** by anomaly behavior.
- On-device inference path with offline capability and low-latency decisions.
- Explainable alerts increase trust and actionability.
- Modular MVVM architecture is ready for scale and enterprise extension.

## Expected Questions and Quick Answers
- **Q: Is this signature-based?**  
  A: No, it is behavior/anomaly based.
- **Q: Is it lightweight?**  
  A: Yes, it is designed for on-device inference with efficient telemetry sampling.
- **Q: Can it scale?**  
  A: Yes, clean architecture + cloud sync hook supports multi-device expansion.
- **Q: Is this real-world deployable?**  
  A: Yes, release artifact is generated and modules are production-structured.
