@echo off
setlocal
cd /d "%~dp0"

echo ==========================================
echo RestoHub - Firebase Google + Build Debug
echo ==========================================
echo.

if not exist "app\google-services.json" (
    echo [ERROR] Falta app\google-services.json
    echo Revisa FIREBASE_GOOGLE_SETUP.md
    echo.
    pause
    exit /b 1
)

echo [1/2] Validando Firebase...
call gradlew.bat :app:verifyFirebaseConfig
if errorlevel 1 (
    echo.
    echo [ERROR] La configuracion de Firebase no es valida.
    pause
    exit /b 1
)

echo.
echo [2/2] Compilando APK debug...
call gradlew.bat :app:assembleDebug
if errorlevel 1 (
    echo.
    echo [ERROR] La compilacion fallo. Revisa el panel Build de Android Studio.
    pause
    exit /b 1
)

echo.
echo [OK] Proyecto compilado correctamente.
echo APK esperado: app\build\outputs\apk\debug\app-debug.apk
echo.
pause
