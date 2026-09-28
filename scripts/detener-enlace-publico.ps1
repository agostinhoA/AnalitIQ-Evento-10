$ErrorActionPreference='Stop'
$projectPath=Split-Path $PSScriptRoot -Parent
$statePath=Join-Path $projectPath '.local/public-tunnel-process.json'
if(!(Test-Path -LiteralPath $statePath)) { Write-Output 'No hay un túnel registrado por esta preparación.'; return }
$state=Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
$tunnelProcess=Get-Process -Id ([int]$state.pid) -ErrorAction SilentlyContinue
if(!$tunnelProcess) { Write-Output 'El enlace temporal ya está detenido.'; return }
$expectedPath=[IO.Path]::GetFullPath((Join-Path $projectPath '.tools/cloudflared.exe'))
$expectedStart=([DateTimeOffset]$state.startTimeUtc).UtcDateTime
if($tunnelProcess.Path -ne $expectedPath -or $tunnelProcess.StartTime.ToUniversalTime() -ne $expectedStart) {
    throw 'El proceso no coincide con el túnel original. No se detuvo ningún proceso.'
}
Stop-Process -Id $tunnelProcess.Id
Write-Output 'Enlace público detenido.'
