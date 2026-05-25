param(
    [string]$PidFile = "app.pid",
    [int]$TimeoutSeconds = 30
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ResolvedPid = Join-Path $ProjectRoot $PidFile

if (!(Test-Path -LiteralPath $ResolvedPid)) {
    Write-Host "PID file not found. Application is not running or was not started by scripts."
    exit 0
}

$PidText = Get-Content -LiteralPath $ResolvedPid -Raw
if (!$PidText.Trim()) {
    Remove-Item -LiteralPath $ResolvedPid -Force
    Write-Host "Empty PID file removed."
    exit 0
}

$AppPid = [int]$PidText
$Process = Get-Process -Id $AppPid -ErrorAction SilentlyContinue
if (!$Process) {
    Remove-Item -LiteralPath $ResolvedPid -Force
    Write-Host "Process $AppPid is not running. PID file removed."
    exit 0
}

Stop-Process -Id $AppPid
$Deadline = (Get-Date).AddSeconds($TimeoutSeconds)

while ((Get-Date) -lt $Deadline) {
    if (!(Get-Process -Id $AppPid -ErrorAction SilentlyContinue)) {
        Remove-Item -LiteralPath $ResolvedPid -Force
        Write-Host "Application stopped."
        exit 0
    }
    Start-Sleep -Milliseconds 500
}

Stop-Process -Id $AppPid -Force
Remove-Item -LiteralPath $ResolvedPid -Force
Write-Host "Application force-stopped."
