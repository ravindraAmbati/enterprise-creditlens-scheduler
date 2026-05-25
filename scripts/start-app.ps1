param(
    [string]$JavaHome = "C:\softwares\jdk-21",
    [string]$JarPath = "target\enterprise-creditlens-scheduler.jar",
    [string]$PidFile = "app.pid",
    [string]$Profile = "",
    [string]$OutLog = "logs\app.out.log",
    [string]$ErrLog = "logs\app.err.log"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$ResolvedJar = Join-Path $ProjectRoot $JarPath
$ResolvedPid = Join-Path $ProjectRoot $PidFile
$ResolvedOutLog = Join-Path $ProjectRoot $OutLog
$ResolvedErrLog = Join-Path $ProjectRoot $ErrLog
$JavaExe = Join-Path $JavaHome "bin\java.exe"

if (!(Test-Path -LiteralPath $JavaExe)) {
    throw "Java executable not found: $JavaExe"
}

if (!(Test-Path -LiteralPath $ResolvedJar)) {
    throw "Application jar not found: $ResolvedJar. Run 'mvn clean package' first."
}

if (Test-Path -LiteralPath $ResolvedPid) {
    $ExistingPid = Get-Content -LiteralPath $ResolvedPid -Raw
    if ($ExistingPid -and (Get-Process -Id ([int]$ExistingPid) -ErrorAction SilentlyContinue)) {
        Write-Host "Application is already running with PID $ExistingPid"
        exit 0
    }
}

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $ResolvedOutLog) | Out-Null

$Arguments = @("-jar", $ResolvedJar)
if ($Profile.Trim()) {
    $Arguments += "--spring.profiles.active=$Profile"
}

$Process = Start-Process `
    -FilePath $JavaExe `
    -ArgumentList $Arguments `
    -WorkingDirectory $ProjectRoot `
    -RedirectStandardOutput $ResolvedOutLog `
    -RedirectStandardError $ResolvedErrLog `
    -WindowStyle Hidden `
    -PassThru

Set-Content -LiteralPath $ResolvedPid -Value $Process.Id
Write-Host "Application started with PID $($Process.Id)"
