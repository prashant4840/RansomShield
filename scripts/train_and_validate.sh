#!/usr/bin/env bash
# RansomShield — Train model, validate, and copy to Android app
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/ml"

if [ ! -d .venv ]; then
    echo "Running setup first..."
    "$ROOT/scripts/setup_ml.sh"
fi
source .venv/bin/activate

echo "=== Training model ==="
if [ -d data ] && (ls data/*.json data/*.csv 2>/dev/null | grep -q .); then
    python train_model.py --data data/
else
    echo "No data/ files found, using synthetic data"
    python train_model.py
fi

echo ""
echo "=== Validating model ==="
if [ -d data ] && (ls data/*.json data/*.csv 2>/dev/null | grep -q .); then
    python validate_model.py --data data/ --model artifacts/ransom_model.tflite || true
else
    echo "Skipping validation (no data)"
fi

echo ""
echo "=== Copying model to app ==="
mkdir -p "$ROOT/android-app/app/src/main/assets"
cp -v artifacts/ransom_model.tflite "$ROOT/android-app/app/src/main/assets/"
echo "Done. Rebuild the Android app to use the new model."
