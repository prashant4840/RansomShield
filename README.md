# RansomShield 🛡️
### AI-Powered Early Warning System for Android Ransomware

> Built in 48 hours for a hackathon. Real-time behavior-based ransomware detection with a futuristic SOC-like dashboard on Android.

![Platform](https://img.shields.io/badge/Platform-Android%208.1+-brightgreen) ![Language](https://img.shields.io/badge/Language-Kotlin%20%7C%20Python-blue) ![ML](https://img.shields.io/badge/ML-TFLite%20%7C%20Anomaly%20Detection-orange) ![Status](https://img.shields.io/badge/Status-Hackathon%20Build-purple)

---

## What is this?

Most ransomware detection happens **too late** — after encryption begins. RansomShield detects suspicious behavior **before** damage occurs using on-device ML inference, adaptive telemetry sampling, and explainable risk scoring.

---

## Project Structure 📁

| Folder | What's inside |
|--------|--------------|
| `android-app/` | Kotlin + Jetpack Compose app — MVVM, monitoring pipeline, explainable alerts |
| `ml/` | Python training scripts, anomaly detection model, TFLite export, validation pipeline |
| `docs/` | Architecture diagrams, pitch deck, wireframes, **step-by-step guide** |
| `scripts/` | One-command setup and build scripts (`setup_ml.sh`, `train_and_validate.sh`, `build_android.sh`) |

---

## Features we built ⚡

- Real-time telemetry stream with anomaly scoring pipeline
- Android signal collectors — CPU, memory, usage, activity heuristics
- Adaptive sampling — faster scans during high-risk windows
- Explainable risk scoring — Low / Medium / High with reasoning
- In-app AI assistant for threat guidance
- One-tap preventive actions — isolation, lock, rollback, safe mode
- Threat simulation mode for live demos
- Cyber dashboard — timeline, app risk ranking, gamified score
- Temporal TFLite inference with windowed reconstruction error
- Encrypted local threat logs + anti-tamper checks
- Firebase Firestore sync for multi-device aggregation
- **Telemetry export** — capture real device behavior for validation
- **Validation pipeline** — benchmark against labeled data (see `docs/validation.md`)

---

## ML Pipeline 🧠

**Python 3.10–3.12 required** (TensorFlow does not support 3.13+). Use `pyenv` or conda if needed.

**Quick (using scripts):**
```bash
./scripts/train_and_validate.sh   # macOS/Linux
# or
./scripts/setup_ml.sh && ./scripts/train_and_validate.sh
```

**Manual — macOS / Linux (Bash):**
```bash
cd ml
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python train_model.py
mkdir -p ../android-app/app/src/main/assets
cp artifacts/ransom_model.tflite ../android-app/app/src/main/assets/
```

**Windows (PowerShell):**
```powershell
cd ml
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python train_model.py
Copy-Item "artifacts\ransom_model.tflite" "..\android-app\app\src\main\assets\ransom_model.tflite" -Force
```

Outputs: `ml/artifacts/metrics_report.json` — precision, recall, F1, false-positive rate

**Train on real telemetry** (exported from app or datasets):
```bash
python train_model.py --data data/
```

**Validate model** against labeled data:
```bash
python validate_model.py --data data/ --model artifacts/ransom_model.tflite
```

- **New?** → `docs/EXACT_STEPS.md` (exact commands + software) or `docs/STEP_BY_STEP_GUIDE.md` (full walkthrough).
- **Validation?** → See `docs/validation.md` for dataset sources and workflow.
- **Too many false alarms?** → See `docs/REDUCING_FALSE_POSITIVES.md`.

---

## Run the Android App 📱

**Prerequisites**
- Android Studio Hedgehog+
- Java 17+ with `JAVA_HOME` configured
- Android 8.1+ device or emulator

**Steps**
1. Open `android-app/` in Android Studio
2. Sync Gradle
3. Run `app` on your device or emulator

**CLI build**
```bash
# Using script
./scripts/build_android.sh

# Or manual
cd android-app && ./gradlew assembleDebug
```
Output APK: `android-app/app/build/outputs/apk/debug/app-debug.apk`

---

## Demo Script 🎬 (2-3 min)

1. Launch app → show baseline **Low Risk**
2. Enable monitoring
3. Toggle **Threat Simulation Mode**
4. Watch score jump with explainable reasons + timeline spike
5. Trigger preventive actions
6. Wrap up with impact and roadmap

---

## What we checked off ✅

- [x] System design — `docs/architecture.md`
- [x] UI wireframe and implementation — `DashboardScreen`
- [x] Core modules — `data`, `domain`, `ml`, `service`, `assistant`
- [x] ML model pipeline — `ml/train_model.py`
- [x] ViewModel + Compose UI integration
- [x] Simulation mode and risk escalation testing
- [x] APK build — `assembleDebug` / `assembleRelease`

---

## CI/CD ⚙️

GitHub Actions at `.github/workflows/android-ci.yml` — runs unit tests and debug/release builds on every push.

---

## Built by

[prashant4840](https://github.com/prashant4840) — CSE Student | Building things that matter 🚀
