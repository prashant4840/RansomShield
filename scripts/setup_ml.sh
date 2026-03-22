#!/usr/bin/env bash
# RansomShield ML setup — creates venv and installs dependencies
set -e
cd "$(dirname "$0")/../ml"
echo "Setting up ML environment in ml/"
python3 -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
echo "Done. Activate with: source ml/.venv/bin/activate"
