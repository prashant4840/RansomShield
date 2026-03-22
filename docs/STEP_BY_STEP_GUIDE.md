# RansomShield — Complete Step-by-Step Guide

Follow these steps from scratch to run the app, capture telemetry, train the model, and validate.

---

## Part 1: Run the Android App

### Step 1.1 — Prerequisites

- **Android Studio** Hedgehog or newer (or any recent version)
- **Java 17+** with `JAVA_HOME` set
- **Android device or emulator** (API 27+ / Android 8.1+)

### Step 1.2 — Open the Project

1. Launch Android Studio
2. Click **Open**
3. Browse to the `Ransomware-Detection` folder
4. Select the **android-app** folder
5. Click **OK**
6. Wait for Gradle sync to finish

### Step 1.3 — Build and Run

1. Connect an Android device (with USB debugging) or start an emulator
2. In Android Studio, choose your device from the run target
3. Click the **Run** (green triangle) button
4. Allow installation if prompted on the device

### Step 1.4 — Grant Permissions (if asked)

- **Usage access** — Go to Settings → Apps → RansomShield → Special app access → Usage access → Enable
- **Notifications** — Allow if prompted

---

## Part 2: Use the App (Basic Flow)

### Step 2.1 — Start Monitoring

1. Open **RansomShield** on your device
2. Turn on **Live Monitoring**
3. You should see a **Low Risk** status and a security score

### Step 2.2 — See Threat Simulation (Demo)

1. With monitoring ON, turn on **Threat Simulation Mode**
2. Watch the risk score rise and the timeline spike
3. Read the **Explainable Alert** reasons
4. Check the **AI Assistant** guidance

### Step 2.3 — Preventive Actions

When risk is High, you can:

- **Kill** — Stop the top risky app (opens App Info)
- **Lock** — Open file access settings
- **Rollback** — Open backup settings
- **Safe Mode** — See how to reboot in Safe Mode

---

## Part 3: Capture Real Telemetry for Validation

### Step 3.1 — Capture Benign Telemetry

1. Turn **off** Threat Simulation Mode
2. Turn **on** Live Monitoring
3. Use the device normally for 5–15 minutes (e.g. open apps, browse, use camera)
4. In the **Telemetry Export** section, tap **Export JSON (benign)** or **Export CSV**
5. Share the file (e.g. Save to Drive, Email, or copy to computer)
6. Save as `benign_export.json` or `benign_export.csv` on your computer

### Step 3.2 — Capture Malicious-Like Telemetry (Simulated)

1. Turn **on** Threat Simulation Mode (leave monitoring on)
2. Wait 5–10 minutes for enough samples
3. Tap **Export JSON (malicious)** or **Export CSV**
4. Share and save as `malicious_sim.json` or `malicious_sim.csv`

### Step 3.3 — Put Files in the Project

1. Create the folder: `RansomShield-Detection/ml/data/` (if it doesn’t exist)
2. Copy your exported files there:
   ```
   ml/data/
   ├── benign_export.json
   └── malicious_sim.json
   ```

---

## Part 4: Set Up the ML Environment

### Step 4.1 — Go to the ML Folder

**macOS / Linux:**
```bash
cd /path/to/RansomShield-Detection/ml
```

**Windows (PowerShell):**
```powershell
cd C:\path\to\RansomShield-Detection\ml
```

### Step 4.2 — Create a Virtual Environment

**macOS / Linux:**
```bash
python3 -m venv .venv
source .venv/bin/activate
```

**Windows (PowerShell):**
```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
```

**Windows (Command Prompt):**
```cmd
python -m venv .venv
.venv\Scripts\activate.bat
```

### Step 4.3 — Install Dependencies

```bash
pip install -r requirements.txt
```

This installs: `numpy`, `scikit-learn`, `tensorflow`.

---

## Part 5: Train the Model

### Step 5.1 — Train with Synthetic Data (No Export Yet)

```bash
cd ml
source .venv/bin/activate   # or .venv\Scripts\activate on Windows
python train_model.py
```

- Outputs: `artifacts/ransom_model.tflite`, `artifacts/metrics_report.json`
- You should see training epochs and final metrics

### Step 5.2 — Train on Your Exported Telemetry

```bash
python train_model.py --data data/
```

Or with specific files:

```bash
python train_model.py --data data/benign_export.json data/malicious_sim.json
```

Optional: more epochs (e.g. 25):

```bash
python train_model.py --data data/ --epochs 25
```

### Step 5.3 — Copy the Model Into the App

**macOS / Linux:**
```bash
mkdir -p ../android-app/app/src/main/assets
cp artifacts/ransom_model.tflite ../android-app/app/src/main/assets/
```

**Windows (PowerShell):**
```powershell
New-Item -ItemType Directory -Force -Path ..\android-app\app\src\main\assets
Copy-Item artifacts\ransom_model.tflite ..\android-app\app\src\main\assets\
```

### Step 5.4 — Rebuild the App

1. In Android Studio, run **Build → Rebuild Project**
2. Run the app again; it will use the new model

---

## Part 6: Validate the Model

### Step 6.1 — Run Validation

```bash
cd ml
source .venv/bin/activate   # or .venv\Scripts\activate on Windows
python validate_model.py --data data/ --model artifacts/ransom_model.tflite
```

### Step 6.2 — Check the Report

Open `ml/artifacts/validation_report.json`:

```json
{
  "data": { "total": 500, "benign": 350, "malicious": 150 },
  "tflite": {
    "precision": 0.92,
    "recall": 0.88,
    "f1": 0.90,
    "false_positive_rate": 0.05
  }
}
```

- **Precision** — Few false alarms (target > 0.85)
- **Recall** — Catches most threats (target > 0.80)
- **F1** — Overall balance (target > 0.82)
- **False positive rate** — Should stay low (target < 0.10)

---

## Part 7: Quick Test (Sample Data)

If you haven’t exported from the app yet, you can use the included samples:

```bash
cd ml
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\Activate.ps1
pip install -r requirements.txt

# Train on sample data
python train_model.py --data data/

# Validate
python validate_model.py --data data/ --model artifacts/ransom_model.tflite
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "No telemetry buffered" | Enable monitoring and wait a few seconds before exporting |
| "No module named 'numpy'" | Run `pip install -r requirements.txt` in the activated venv |
| "No data found" | Ensure `ml/data/` has `.json` or `.csv` files |
| "No benign samples" | Add at least one export with simulation OFF (label 0) |
| Model not updating in app | Copy the new `.tflite` into `assets/` and rebuild |
| Gradle sync failed | Use Java 17; File → Invalidate Caches → Restart |

---

## One-Page Cheat Sheet

```text
1. RUN APP          → Open android-app in Android Studio, Run
2. USE APP          → Enable monitoring, try simulation mode
3. EXPORT           → Export JSON (benign) and (malicious), save to ml/data/
4. ML (script)      → ./scripts/train_and_validate.sh  (does setup + train + validate + copy)
5. ML (manual)      → cd ml && source .venv/bin/activate && pip install -r requirements.txt
6. TRAIN            → python train_model.py --data data/
7. VALIDATE         → python validate_model.py --data data/
8. REBUILD APP      → Rebuild in Android Studio or ./scripts/build_android.sh
```
