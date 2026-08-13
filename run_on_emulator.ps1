# Falcon Player - Build, Deploy & Launch on Android Studio Emulator
param (
    [string]$AvdName = "Pixel_5"
)

$adbPath = "C:\Users\ragib\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"

Write-Host "1. Building Debug APK..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed!"
    exit $LASTEXITCODE
}

Write-Host "2. Checking for active Android Studio emulator..." -ForegroundColor Cyan
$devicesOutput = & $adbPath devices | Out-String
if ($devicesOutput -match "emulator-\d+") {
    $targetDevice = $Matches[0]
    Write-Host "Found active emulator: $targetDevice" -ForegroundColor Green
} else {
    Write-Host "Starting emulator $AvdName..." -ForegroundColor Yellow
    android emulator start $AvdName
    $targetDevice = "emulator-5554"
}

Write-Host "3. Installing APK on $targetDevice..." -ForegroundColor Cyan
& $adbPath -s $targetDevice install -r $apkPath

Write-Host "4. Launching Falcon Player..." -ForegroundColor Cyan
& $adbPath -s $targetDevice shell am start -n com.example.falconplayer/.MainActivity

Write-Host "Successfully launched Falcon Player on Android Studio emulator ($targetDevice)!" -ForegroundColor Green
