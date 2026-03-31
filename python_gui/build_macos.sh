#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ICON_PATH="$REPO_ROOT/python_gui/assets/MyDailyCash.icns"

cd "$REPO_ROOT"

zsh "$REPO_ROOT/python_gui/generate_macos_icon.sh"

python3 -m PyInstaller \
  --noconfirm \
  --clean \
  --windowed \
  --onedir \
  --name MyDailyCash \
  --icon "$ICON_PATH" \
  --add-data "$REPO_ROOT/index.html:." \
  "$REPO_ROOT/python_gui/app.py"
