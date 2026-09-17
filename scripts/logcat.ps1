param(
    [string]$Package = "com.any.app"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$logDirectory = Join-Path $projectRoot "logs"
$adb = "C:\Users\HWT\AppData\Local\Android\Sdk\platform-tools\adb.exe"

New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$logPath = Join-Path $logDirectory "logcat-$timestamp.log"

if (-not (Test-Path -LiteralPath $adb)) {
    throw "ADB not found: $adb"
}

Write-Host "Capturing Logcat for $Package"
Write-Host "Log file: $logPath"
Write-Host "Press Ctrl+C to stop."

& $adb logcat -v time 2>&1 | Tee-Object -FilePath $logPath
