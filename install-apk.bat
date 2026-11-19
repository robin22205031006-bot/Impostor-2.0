@echo off
setlocal

set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK_PATH%" (
  echo APK not found. Building debug APK first...
  call npm run build
  call gradlew.bat assembleDebug
)

where adb >nul 2>nul
if errorlevel 1 (
  echo Error: adb is not installed or not in PATH.
  echo You can copy "%APK_PATH%" directly to your Android phone and open it to install.
  exit /b 1
)

echo Installing %APK_PATH% to connected Android device...
adb install -r "%APK_PATH%"
echo Done! Launch Remix Impostor on your device.
