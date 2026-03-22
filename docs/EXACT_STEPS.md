# RansomShield — Exact Steps to Follow

Where to run each command and which software to use.

---

## Software You Need

| Software | Purpose |
|----------|---------|
| **Terminal** (macOS/Linux) or **PowerShell** (Windows) | Run ML commands |
| **Android Studio** | Build and run the Android app |
| **Python 3.10, 3.11, or 3.12** | ML training (TensorFlow does not support 3.13+) |

---

## Part A: Run the Android App

### Step A1 — Open the project

| Where | What to do |
|-------|------------|
| **Android Studio** | 1. Launch Android Studio<br>2. File → Open (or welcome screen: Open)<br>3. Go to your project folder (e.g. `Ransomware-Detection`)<br>4. Select the **android-app** folder (not the root)<br>5. Click Open<br>6. Wait for "Gradle sync finished" at the bottom |

### Step A2 — Connect device or start emulator

| Where | What to do |
|-------|------------|
| **Android Studio** | 1. Connect an Android phone via USB (with USB debugging enabled), or<br>2. AVD Manager → Create/start an emulator |
| **Phone** | If using phone: enable Developer Options, turn on USB debugging |

### Step A3 — Run the app

| Where | What to do |
|-------|------------|
| **Android Studio** | 1. Select your device in the run target dropdown (top toolbar)<br>2. Click the green **Run** (▶) button<br>3. Approve installation on the device if asked |

### Step A4 — Grant permissions (if asked)

| Where | What to do |
|-------|------------|
| **Phone/Emulator** | Settings → Apps → RansomShield → Special app access → Usage access → turn **On** |

---

## Part B: Use the app

| Where | What to do |
|-------|------------|
| **Phone/Emulator** | 1. Open **RansomShield**<br>2. Turn on **Live Monitoring**<br>3. (Optional) Turn on **Threat Simulation Mode** to see a demo |

---

## Part C: ML setup (first time only)

### Step C1 — Open Terminal / PowerShell

| Where | What to do |
|-------|------------|
| **Terminal** (macOS/Linux) or **PowerShell** (Windows) | Open a new window |

### Step C2 — Go to project root

| Where | Command |
|-------|---------|
| **Terminal** | `cd /path/to/Ransomware-Detection` |
| **PowerShell** | `cd C:\path\to\Ransomware-Detection` |

Replace with the real path to your project folder.

### Step C3 — Go into `ml` and create venv

| Where | Command |
|-------|---------|
| **Terminal (macOS/Linux)** | `cd ml` then `python3 -m venv .venv` |
| **PowerShell (Windows)** | `cd ml` then `python -m venv .venv` |

### Step C4 — Activate venv

| Where | Command |
|-------|---------|
| **Terminal (macOS/Linux)** | `source .venv/bin/activate` |
| **PowerShell (Windows)** | `.\.venv\Scripts\Activate.ps1` |

You should see `(.venv)` at the start of the line.

### Step C5 — Install dependencies

| Where | Command |
|-------|---------|
| **Terminal or PowerShell** | `pip install -r requirements.txt` |

Stay in the `ml` folder with venv activated.

---

## Part D: Train the model

### Step D1 — Make sure venv is active

| Where | What to do |
|-------|------------|
| **Terminal / PowerShell** | You must be in `ml` with `(.venv)` shown. If not: `cd ml` and run the activate command from C4 again. |

### Step D2 — Train (choose one)

| Where | Command | When to use |
|-------|---------|-------------|
| **Terminal / PowerShell** | `python train_model.py` | No exported data yet (synthetic) |
| **Terminal / PowerShell** | `python train_model.py --data data/` | You have JSON/CSV in `ml/data/` |

### Step D3 — Check output

| Where | What to check |
|-------|---------------|
| **Terminal / PowerShell** | You should see training epochs and "Training complete." at the end |
| **File system** | `ml/artifacts/ransom_model.tflite` exists |

---

## Part E: Copy model into the app

### Step E1 — Copy the model file

| Where | Command |
|-------|---------|
| **Terminal (macOS/Linux)** | `cp ml/artifacts/ransom_model.tflite android-app/app/src/main/assets/` |
| **PowerShell (Windows)** | `Copy-Item ml\artifacts\ransom_model.tflite android-app\app\src\main\assets\` |

Run this from the **project root** (parent of `ml` and `android-app`).

### Step E2 — Rebuild app

| Where | What to do |
|-------|------------|
| **Android Studio** | Build → Rebuild Project, then run the app again |

---

## Part F: Validate (optional)

| Where | Command |
|-------|---------|
| **Terminal / PowerShell** | `cd ml` (from project root), ensure venv is active, then:<br>`python validate_model.py --data data/` |

Use this only if you have data in `ml/data/`.

---

## Part G: Export telemetry from the app (for real data)

| Where | What to do |
|-------|------------|
| **Phone/Emulator** | 1. Turn on Live Monitoring<br>2. Use device normally (benign) or with simulation (malicious)<br>3. Scroll to **Telemetry Export**<br>4. Tap **Export JSON (benign)** or **Export JSON (malicious)**<br>5. Save/share file to your computer |
| **File system** | Put the exported file in `ml/data/` (e.g. `benign_export.json`, `malicious_sim.json`) |

---

## Quick reference table

| Step | Software | Location | Command/Action |
|------|----------|----------|----------------|
| Open project | Android Studio | — | Open `android-app` folder |
| Run app | Android Studio | — | Click Run ▶ |
| ML setup | Terminal/PowerShell | Project root → `ml` | `python3 -m venv .venv` (or `python` on Windows) |
| Activate venv | Terminal | `ml` | `source .venv/bin/activate` |
| Activate venv | PowerShell | `ml` | `.\.venv\Scripts\Activate.ps1` |
| Install deps | Terminal/PowerShell | `ml` | `pip install -r requirements.txt` |
| Train | Terminal/PowerShell | `ml` | `python train_model.py` or `python train_model.py --data data/` |
| Copy model | Terminal | Project root | `cp ml/artifacts/ransom_model.tflite android-app/app/src/main/assets/` |
| Copy model | PowerShell | Project root | `Copy-Item ml\artifacts\ransom_model.tflite android-app\app\src\main\assets\` |
| Rebuild app | Android Studio | — | Build → Rebuild Project |

---

## One-line sequence (Terminal, from project root)

```bash
cd ml && python3 -m venv .venv && source .venv/bin/activate && pip install -r requirements.txt && python train_model.py --data data/ && cd .. && cp ml/artifacts/ransom_model.tflite android-app/app/src/main/assets/
```

Then in Android Studio: Build → Rebuild Project → Run.
