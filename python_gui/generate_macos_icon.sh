#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ASSETS_DIR="$SCRIPT_DIR/assets"
SVG_ICON="$ASSETS_DIR/macos_app_icon.svg"
PNG_ICON="$ASSETS_DIR/macos_app_icon.png"
ICONSET_DIR="$ASSETS_DIR/MyDailyCash.iconset"
ICNS_ICON="$ASSETS_DIR/MyDailyCash.icns"

mkdir -p "$ASSETS_DIR"
rm -rf "$ICONSET_DIR"
mkdir -p "$ICONSET_DIR"

qlmanage -t -s 1024 -o "$ASSETS_DIR" "$SVG_ICON" >/dev/null
mv -f "$ASSETS_DIR/macos_app_icon.svg.png" "$PNG_ICON"

for size in 16 32 128 256 512; do
  sips -z "$size" "$size" "$PNG_ICON" --out "$ICONSET_DIR/icon_${size}x${size}.png" >/dev/null
  double_size=$((size * 2))
  sips -z "$double_size" "$double_size" "$PNG_ICON" --out "$ICONSET_DIR/icon_${size}x${size}@2x.png" >/dev/null
done

iconutil -c icns "$ICONSET_DIR" -o "$ICNS_ICON"
