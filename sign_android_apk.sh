#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEFAULT_BUILD_TOOLS="/opt/homebrew/share/android-commandlinetools/build-tools/35.0.0"
DEFAULT_KEYSTORE="$SCRIPT_DIR/.release-signing/mydailycash-release.jks"
DEFAULT_ALIAS="mydailycash-release"
DEFAULT_STORE_PASS="5ccf8a99bace8c0866b0bd4f"
DEFAULT_KEY_PASS="5ccf8a99bace8c0866b0bd4f"
DEFAULT_JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"

usage() {
  cat <<'EOF'
Usage:
  ./sign_android_apk.sh /path/to/input.apk [output.apk] [release_tag]

Examples:
  ./sign_android_apk.sh /tmp/app-release.apk
  ./sign_android_apk.sh /tmp/app-release.apk /tmp/MyDailyCash-signed.apk
  ./sign_android_apk.sh /tmp/app-release.apk /tmp/MyDailyCash-signed.apk v1.0.6

Environment overrides:
  JAVA_HOME
  BUILD_TOOLS
  KEYSTORE
  ALIAS
  STORE_PASS
  KEY_PASS
  GITHUB_REPO
EOF
}

if [[ $# -lt 1 || $# -gt 3 ]]; then
  usage
  exit 1
fi

INPUT_APK="$1"
OUTPUT_APK="${2:-${INPUT_APK:r}-signed.apk}"
RELEASE_TAG="${3:-}"

JAVA_HOME="${JAVA_HOME:-$DEFAULT_JAVA_HOME}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

BUILD_TOOLS="${BUILD_TOOLS:-$DEFAULT_BUILD_TOOLS}"
KEYSTORE="${KEYSTORE:-$DEFAULT_KEYSTORE}"
ALIAS="${ALIAS:-$DEFAULT_ALIAS}"
STORE_PASS="${STORE_PASS:-$DEFAULT_STORE_PASS}"
KEY_PASS="${KEY_PASS:-$DEFAULT_KEY_PASS}"
GITHUB_REPO="${GITHUB_REPO:-goldshoot0720/mydailycash}"

ZIPALIGN="$BUILD_TOOLS/zipalign"
APKSIGNER="$BUILD_TOOLS/apksigner"
ALIGNED_APK="${OUTPUT_APK:r}-aligned.apk"

if [[ ! -f "$INPUT_APK" ]]; then
  echo "Input APK not found: $INPUT_APK" >&2
  exit 1
fi

if [[ ! -f "$KEYSTORE" ]]; then
  echo "Keystore not found: $KEYSTORE" >&2
  exit 1
fi

if [[ ! -x "$ZIPALIGN" ]]; then
  echo "zipalign not found: $ZIPALIGN" >&2
  exit 1
fi

if [[ ! -x "$APKSIGNER" ]]; then
  echo "apksigner not found: $APKSIGNER" >&2
  exit 1
fi

mkdir -p "$(dirname "$OUTPUT_APK")"

rm -f "$ALIGNED_APK" "$OUTPUT_APK"

"$ZIPALIGN" -f -p 4 "$INPUT_APK" "$ALIGNED_APK"

"$APKSIGNER" sign \
  --ks "$KEYSTORE" \
  --ks-key-alias "$ALIAS" \
  --ks-pass pass:"$STORE_PASS" \
  --key-pass pass:"$KEY_PASS" \
  --out "$OUTPUT_APK" \
  "$ALIGNED_APK"

"$APKSIGNER" verify --verbose --print-certs "$OUTPUT_APK"

rm -f "$ALIGNED_APK"

echo "Signed APK: $OUTPUT_APK"

if [[ -n "$RELEASE_TAG" ]]; then
  ASSET_NAME="$(basename "$OUTPUT_APK")"
  gh release upload "$RELEASE_TAG" "$OUTPUT_APK#$ASSET_NAME" --clobber --repo "$GITHUB_REPO"
  echo "Uploaded $ASSET_NAME to release $RELEASE_TAG"
fi
