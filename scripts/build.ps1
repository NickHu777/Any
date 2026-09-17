param(
    [ValidateSet("assembleDebug", "compileDebugKotlin", "testDebugUnitTest")]
    [string]$Task = "assembleDebug"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$logDirectory = Join-Path $projectRoot "logs"
$taskJdk = "D:\1HWT\SoftWare\Android Studio\jbr"

New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$logPath = Join-Path $logDirectory "build-$timestamp.log"

Push-Location $projectRoot
try {
    $env:JAVA_HOME = $taskJdk
    "Any build log" | Set-Content -LiteralPath $logPath -Encoding UTF8
    "Task: $Task" | Add-Content -LiteralPath $logPath -Encoding UTF8
    "Started: $(Get-Date -Format o)" | Add-Content -LiteralPath $logPath -Encoding UTF8
    "" | Add-Content -LiteralPath $logPath -Encoding UTF8

    & .\gradlew.bat ":app:$Task" --console=plain 2>&1 |
        Tee-Object -FilePath $logPath -Append
    $exitCode = $LASTEXITCODE

    "" | Add-Content -LiteralPath $logPath -Encoding UTF8
    "ExitCode: $exitCode" | Add-Content -LiteralPath $logPath -Encoding UTF8
    "Finished: $(Get-Date -Format o)" | Add-Content -LiteralPath $logPath -Encoding UTF8
    Write-Host "Build log: $logPath"
    exit $exitCode
}
finally {
    Pop-Location
}
