@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

set "PREBUILT_APK=%CD%\apk\Impostor.apk"
set "BUILT_APK=%CD%\app\build\outputs\apk\debug\app-debug.apk"

if /I "%~1"=="--rebuild" goto rebuild
if not exist "%PREBUILT_APK%" goto rebuild
goto install

:rebuild
echo ==^> Building Impostor APK from source...
call gradlew.bat assembleDebug --no-daemon
if errorlevel 1 (
    echo ERROR: Gradle build failed. Make sure JDK 17+ and Android SDK are installed.
    exit /b 1
)
if not exist "%CD%\apk" mkdir "%CD%\apk"
copy /Y "%BUILT_APK%" "%PREBUILT_APK%" >nul

:install
echo ==^> Ready APK: %PREBUILT_APK%

where adb >nul 2>nul
if %errorlevel%==0 (
    for /f "skip=1 tokens=1,2" %%A in ('adb devices') do (
        if "%%B"=="device" (
            echo ==^> Installing onto connected Android device %%A...
            adb -s %%A install -r "%PREBUILT_APK%"
            if not errorlevel 1 (
                adb -s %%A shell monkey -p com.aistudio.impostorgame.qvnxkp -c android.intent.category.LAUNCHER 1 >nul 2>nul
                echo ==^> Installed and launched!
                exit /b 0
            )
        )
    )
)

echo.
echo No USB/wireless ADB device detected.
echo Copy this APK to your Android phone and tap it to install:
echo   %PREBUILT_APK%
