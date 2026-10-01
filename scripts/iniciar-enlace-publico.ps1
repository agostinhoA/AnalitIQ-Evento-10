param([switch]$Abrir)
$ErrorActionPreference='Stop'
$projectPath=Split-Path $PSScriptRoot -Parent
$localPath=Join-Path $projectPath '.local'
$exe=Join-Path $projectPath '.tools/cloudflared.exe'
$urlPath=Join-Path $localPath 'public-demo-url.txt'
$statePath=Join-Path $localPath 'public-tunnel-process.json'
$logPath=Join-Path $localPath 'public-tunnel.err.log'
$origin='http://127.0.0.1:8082/analitiq/deudas'
$engine=(Get-Process -Id $PID).Path
function Test-AnalitIQ([string]$Url) {
    try {
        $response=Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 8
        return ($response.StatusCode -eq 200 -and $response.Content -match 'AnalitIQ')
    } catch { return $false }
}
function Show-Link([string]$Url) {
    Write-Output "Enlace para compartir: $Url"
    Write-Output 'Mantener esta computadora encendida, con Internet y sin suspender.'
    Write-Output 'El enlace es temporal y cambia cuando se crea otro tunel.'
    if($Abrir) { Start-Process $Url }
}
$mutex=[Threading.Mutex]::new($false,'Local\AnalitIQ-public-demo-launch')
$locked=$false
try {
    $locked=$mutex.WaitOne(0)
    if(!$locked) { throw 'Ya hay otro inicio del enlace en curso.' }
    foreach($required in @($exe,(Join-Path $localPath 'public-demo.properties'))) {
        if(!(Test-Path -LiteralPath $required)) { throw "Falta la configuracion local: $required" }
    }
    if(!(Test-AnalitIQ $origin)) {
        $mysql=Get-Service -Name wampmysqld64 -ErrorAction Stop
        if($mysql.Status -ne 'Running') {
            try { Start-Service -Name wampmysqld64 -ErrorAction Stop }
            catch {
                Write-Output 'Windows solicitara permiso para iniciar MySQL de WAMP.'
                $admin=Start-Process -FilePath $engine -Verb RunAs -WindowStyle Hidden -ArgumentList '-NoProfile -Command "Start-Service -Name wampmysqld64 -ErrorAction Stop"' -PassThru
                if(!$admin.WaitForExit(30000)) { throw 'MySQL no termino de iniciarse. Revisar el permiso de Windows.' }
            }
            if((Get-Service wampmysqld64).Status -ne 'Running') { throw 'MySQL sigue detenido.' }
        }
        $tcp=[Net.Sockets.TcpClient]::new()
        $portOpen=$false
        try { $tcp.Connect('127.0.0.1',8082); $portOpen=$true } catch [Net.Sockets.SocketException] { } finally { $tcp.Dispose() }
        if(!$portOpen) {
            if(!$env:JAVA_HOME) { $env:JAVA_HOME=Join-Path $env:ProgramFiles 'Java/jdk-17' }
            $runner=Join-Path $PSScriptRoot 'ejecutar-tomcat.ps1'
            $tomcat=Join-Path $projectPath '.tools/apache-tomcat-9.0.122'
            $base=Join-Path $localPath 'tomcat-public'
            $config=Join-Path $localPath 'public-demo.properties'
            $arguments=@('-NoProfile','-ExecutionPolicy','Bypass','-File',('"'+$runner+'"'),'-TomcatHome',('"'+$tomcat+'"'),'-Port','8082','-BaseDirectory',('"'+$base+'"'),'-ConfigFile',('"'+$config+'"'))
            Start-Process -FilePath $engine -WindowStyle Hidden -ArgumentList $arguments -RedirectStandardOutput (Join-Path $localPath 'tomcat-public-restart.out.log') -RedirectStandardError (Join-Path $localPath 'tomcat-public-restart.err.log') | Out-Null
        }
        $ready=$false
        for($i=0;$i -lt 10;$i++) {
            if(Test-AnalitIQ $origin) { $ready=$true; break }
            Start-Sleep -Seconds 2
        }
        if(!$ready) { throw 'La demo en 8082 no responde. Revisar los logs de Tomcat antes de publicar.' }
    }
    if(Test-Path -LiteralPath $statePath) {
        $state=Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
        $existing=Get-Process -Id ([int]$state.pid) -ErrorAction SilentlyContinue
        if($existing) {
            $expectedStart=([DateTimeOffset]$state.startTimeUtc).UtcDateTime
            if($existing.Path -ne $exe -or $existing.StartTime.ToUniversalTime() -ne $expectedStart) {
                throw 'El proceso registrado no coincide. No se detuvo ningun proceso.'
            }
            if(Test-Path -LiteralPath $urlPath) {
                $savedUrl=(Get-Content -LiteralPath $urlPath -Raw).Trim()
                if($savedUrl -match '^https://[a-z0-9-]+\.trycloudflare\.com/analitiq/$' -and (Test-AnalitIQ ($savedUrl+'deudas'))) {
                    Show-Link $savedUrl
                    return
                }
            }
            & (Join-Path $PSScriptRoot 'detener-enlace-publico.ps1')
            $existing.WaitForExit(5000) | Out-Null
        }
    }
    $tunnel=Start-Process -FilePath $exe -ArgumentList 'tunnel --url http://127.0.0.1:8082 --no-autoupdate --protocol http2 --metrics 127.0.0.1:49312' -WindowStyle Hidden -RedirectStandardOutput (Join-Path $localPath 'public-tunnel.out.log') -RedirectStandardError $logPath -PassThru
    @{pid=$tunnel.Id;startTimeUtc=$tunnel.StartTime.ToUniversalTime().ToString('o');executable=$exe} | ConvertTo-Json | Set-Content -LiteralPath $statePath
    $publicUrl=$null
    for($i=0;$i -lt 40;$i++) {
        $tunnel.Refresh()
        if($tunnel.HasExited) { throw 'El tunel se detuvo. Revisar .local/public-tunnel.err.log.' }
        $logText=Get-Content -LiteralPath $logPath -Raw
        if([string]::IsNullOrEmpty($logText)) { Start-Sleep -Seconds 2; continue }
        $match=[regex]::Match($logText,'https://[a-z0-9-]+\.trycloudflare\.com')
        if($match.Success) {
            $publicUrl=$match.Value+'/analitiq/'
            if(Test-AnalitIQ ($publicUrl+'deudas')) {
                Set-Content -LiteralPath $urlPath -Value $publicUrl
                Show-Link $publicUrl
                return
            }
        }
        Start-Sleep -Seconds 2
    }
    throw 'Todavia no se pudo verificar el acceso publico. Revisar Internet y volver a ejecutar el iniciador.'
} finally {
    if($locked) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
}
