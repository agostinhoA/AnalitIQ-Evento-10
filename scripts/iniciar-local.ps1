param([switch]$Compilar)

$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$javaHome = 'C:\Program Files\Java\jdk-17'
$maven = 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd'
$tomcatHome = Join-Path $project '.tools/apache-tomcat-9.0.122'
$config = Join-Path $project '.local/analitiq.properties'

if (!(Test-Path -LiteralPath (Join-Path $javaHome 'bin/java.exe'))) { throw 'No se encontró JDK 17.' }
if (!(Test-Path -LiteralPath (Join-Path $tomcatHome 'bin/catalina.bat'))) { throw 'No se encontró Tomcat 9 portable en .tools.' }
if (!(Test-Path -LiteralPath $config)) { throw 'Falta .local/analitiq.properties. Ejecutar primero scripts/configurar-local.ps1.' }

$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"
if ($Compilar) {
    if (!(Test-Path -LiteralPath $maven)) { throw 'No se encontró Maven dentro de NetBeans.' }
    Push-Location $project
    try {
        & $maven '-Dmaven.repo.local=.tools/m2' clean verify
        if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación.' }
    } finally { Pop-Location }
}

$listener = [Net.Sockets.TcpClient]::new()
try {
    $listener.Connect('127.0.0.1', 8080)
    if ($listener.Connected) { throw 'El puerto 8080 está ocupado. Detener el servicio Tomcat9 instalado antes de iniciar AnalitIQ.' }
} catch [Net.Sockets.SocketException] {
    # Sin servicio en 8080: la instancia local puede iniciarse.
} finally {
    $listener.Dispose()
}

& (Join-Path $PSScriptRoot 'ejecutar-tomcat.ps1') -TomcatHome $tomcatHome -Port 8080
