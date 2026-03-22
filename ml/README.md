# RansomShield ML Pipeline

## Python Version

**TensorFlow requires Python 3.10–3.12.** If you have Python 3.13+, use `pyenv` or a conda env:

```bash
# Using pyenv
pyenv install 3.12
pyenv local 3.12
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Quick Start

```bash
# Setup (one-time)
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\Activate.ps1
pip install -r requirements.txt

# Train (synthetic data)
python train_model.py

# Train (real data)
python train_model.py --data data/

# Validate
python validate_model.py --data data/
```

Or use the project scripts: `../scripts/train_and_validate.sh`
