@echo off
setlocal

set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK_PATH%" if exist "apk\Impostor.apk" (
  set APK_PATH=apk\Impostor.apk
)

if not exist "%APK_PATH%" (
  echo APK not found. Building debug APK first...
  call npm run build
  if exist app\src\main\assets\www rmdir /s /q app\src\main\assets\www
  mkdir app\src\main\assets\www
  xcopy /E /I /Y dist\* app\src\main\assets\www\ >nul
  call gradlew.bat assembleDebug
  set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
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
