# AI-Powered Early Warning System for Android Ransomware

## 1) System Design
- **Signal Layer:** Collect CPU/memory/IO bursts, file mutation frequency, accessibility misuse signals, suspicious permission profile.
- **Detection Layer:** On-device anomaly score from lightweight engine (TFLite-ready adapter).
- **Response Layer:** Explainable alerting + one-tap mitigation actions.
- **Experience Layer:** SOC-like dashboard with timeline, app risk board, AI assistant guidance.

## 2) Architecture (Clean MVVM)
- `data`: telemetry repository and data acquisition adapters.
- `ml`: inference engine abstraction and model runtime.
- `domain`: risk models and decision contracts.
- `service`: foreground monitoring service + preventive actions.
- `ui`: Compose dashboard, charts, threat timeline, controls.
- `assistant`: natural-language recommendations from risk context.

## 3) Event-Driven Detection Pipeline
1. Sensor snapshot emitted every 1.2 sec.
2. Features normalized and scored.
3. Risk bucket computed (Low/Medium/High).
4. Explainability reasons generated.
5. Alerts + dashboard + action recommendations updated.

## 4) Threat Simulation Mode (Demo)
- Simulates pre-encryption behavior spikes.
- Shows timeline escalation and explainable cause labels.
- Enables judge-friendly end-to-end workflow without real malware.

## 5) Production Hardening Roadmap
- Integrate Android `UsageStatsManager` and `AccessibilityService` telemetry.
- Replace heuristic model with Autoencoder/LSTM TFLite inference.
- Add encrypted local event store + signed cloud sync (Firebase optional).
- Implement policy engine and enterprise admin controls.
