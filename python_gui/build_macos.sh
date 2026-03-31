#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$REPO_ROOT"

python3 -m PyInstaller \
  --noconfirm \
  --clean \
  --windowed \
  --onedir \
  --name MyDailyCash \
  --add-data "$REPO_ROOT/index.html:." \
  "$REPO_ROOT/python_gui/app.py"
