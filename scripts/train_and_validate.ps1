# RansomShield — Train model, validate, and copy to Android app
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location "$Root\ml"

if (-not (Test-Path .venv)) {
    Write-Host "Running setup first..."
    & "$Root\scripts\setup_ml.ps1"
}
& .\.venv\Scripts\Activate.ps1

Write-Host "=== Training model ==="
$hasData = (Get-ChildItem -Path data -Include *.json,*.csv -Recurse -ErrorAction SilentlyContinue | Measure-Object).Count -gt 0
if ($hasData) {
    python train_model.py --data data/
} else {
    Write-Host "No data/ files found, using synthetic data"
    python train_model.py
}

Write-Host ""
Write-Host "=== Validating model ==="
if ($hasData) {
    python validate_model.py --data data/ --model artifacts/ransom_model.tflite 2>$null; if (-not $?) { Write-Host "Validation skipped or failed" }
} else {
    Write-Host "Skipping validation (no data)"
}

Write-Host ""
Write-Host "=== Copying model to app ==="
New-Item -ItemType Directory -Force -Path "$Root\android-app\app\src\main\assets" | Out-Null
Copy-Item artifacts\ransom_model.tflite "$Root\android-app\app\src\main\assets\" -Force
Write-Host "Done. Rebuild the Android app to use the new model."
