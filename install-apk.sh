#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

PREBUILT_APK="$ROOT_DIR/apk/Impostor.apk"
BUILT_APK="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"

if [ "${1:-}" = "--rebuild" ] || [ ! -f "$PREBUILT_APK" ]; then
  echo "==> Building Impostor APK from source..."
  chmod +x ./gradlew
  ./gradlew assembleDebug --no-daemon
  mkdir -p "$ROOT_DIR/apk"
  cp "$BUILT_APK" "$PREBUILT_APK"
fi

APK_TO_INSTALL="$PREBUILT_APK"
echo "==> Ready APK: $APK_TO_INSTALL"

if command -v adb >/dev/null 2>&1; then
  DEVICES="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
  if [ -n "$DEVICES" ]; then
    echo "==> Installing onto connected Android device..."
    adb install -r "$APK_TO_INSTALL"
    echo "==> Installed! Launching Impostor..."
    adb shell monkey -p com.aistudio.impostorgame.qvnxkp -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
    echo "==> Done."
    exit 0
  fi
fi

if command -v termux-open >/dev/null 2>&1; then
  echo "==> Opening APK installer via Termux..."
  termux-open "$APK_TO_INSTALL"
  exit 0
fi

echo ""
echo "Tap or transfer this file to your Android phone to install:"
echo "  $APK_TO_INSTALL"
