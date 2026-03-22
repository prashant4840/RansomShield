# RansomShield ML setup — creates venv and installs dependencies
$ErrorActionPreference = "Stop"
Set-Location "$PSScriptRoot\..\ml"
Write-Host "Setting up ML environment in ml/"
python -m venv .venv
& .\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r requirements.txt
Write-Host "Done. Activate with: .\ml\.venv\Scripts\Activate.ps1"
