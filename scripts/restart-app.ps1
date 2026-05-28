param(
    [string]$JavaHome = "C:\softwares\jdk-17",
    [string]$JarPath = "target\enterprise-creditlens-scheduler.jar",
    [string]$PidFile = "app.pid",
    [string]$Profile = "",
    [string]$OutLog = "logs\app.out.log",
    [string]$ErrLog = "logs\app.err.log"
)

$ErrorActionPreference = "Stop"

& (Join-Path $PSScriptRoot "stop-app.ps1") -PidFile $PidFile
& (Join-Path $PSScriptRoot "start-app.ps1") `
    -JavaHome $JavaHome `
    -JarPath $JarPath `
    -PidFile $PidFile `
    -Profile $Profile `
    -OutLog $OutLog `
    -ErrLog $ErrLog
