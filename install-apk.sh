#!/usr/bin/env bash
set -euo pipefail

APK_PATH="apk/Impostor.apk"

if [ ! -f "$APK_PATH" ]; then
  APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

if [ ! -f "$APK_PATH" ]; then
  echo "APK not found. Building debug APK first..."
  npm run build
  ./gradlew assembleDebug
  mkdir -p apk
  cp app/build/outputs/apk/debug/app-debug.apk apk/Impostor.apk
  APK_PATH="apk/Impostor.apk"
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "Error: adb is not installed or not in PATH."
  echo "You can still copy '$APK_PATH' directly to your Android device and tap it to install."
  exit 1
fi

echo "Installing $APK_PATH to connected Android device..."
adb install -r "$APK_PATH"
echo "Done! Launch 'Remix Impostor' on your device."
