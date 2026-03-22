# RansomShield Project Structure

```
Ransomware-Detection/
├── android-app/                    # Android app (Kotlin + Compose)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── assets/             # ransom_model.tflite
│   │   │   ├── java/.../earlywarning/
│   │   │   │   ├── data/           # TelemetryRepository, SignalCollectors, TelemetryExporter
│   │   │   │   ├── domain/         # Models (TelemetrySample, ThreatAssessment)
│   │   │   │   ├── ml/             # ThreatInferenceEngine, TFLiteScorer
│   │   │   │   ├── service/        # PreventiveActionManager, MonitoringForegroundService
│   │   │   │   ├── ui/             # ViewModel, DashboardScreen, components
│   │   │   │   ├── assistant/      # ThreatAssistant
│   │   │   │   └── security/       # AntiTamperGuard, EncryptedThreatLogStore
│   │   │   └── res/
│   │   └── build.gradle.kts
│   └── gradlew
├── ml/
│   ├── data/                       # Telemetry JSON/CSV (export from app or datasets)
│   │   ├── sample_benign.json
│   │   ├── sample_malicious.json
│   │   └── README.md
│   ├── artifacts/                  # Output: ransom_model.tflite, metrics_report.json
│   ├── load_telemetry.py
│   ├── train_model.py
│   ├── validate_model.py
│   ├── requirements.txt
│   └── README.md
├── scripts/
│   ├── setup_ml.sh                 # Create venv, install deps
│   ├── setup_ml.ps1
│   ├── train_and_validate.sh       # Train, validate, copy model to app
│   ├── train_and_validate.ps1
│   └── build_android.sh
├── docs/
│   ├── INDEX.md                    # Documentation hub
│   ├── STEP_BY_STEP_GUIDE.md       # Full walkthrough
│   ├── architecture.md
│   ├── validation.md
│   ├── REDUCING_FALSE_POSITIVES.md
│   └── pitch.md
├── .github/workflows/
│   └── android-ci.yml
├── README.md
└── PROJECT_STRUCTURE.md
```
