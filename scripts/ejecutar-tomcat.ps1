param([Parameter(Mandatory=$true)][string]$TomcatHome,[int]$Port = 8080)
$ErrorActionPreference='Stop'
$project=Split-Path $PSScriptRoot -Parent
$TomcatHome=(Resolve-Path $TomcatHome).Path
if (!$env:JAVA_HOME -and !$env:JRE_HOME) {
    $javaSettings = & java -XshowSettings:properties -version 2>&1
    $javaHomeLine = $javaSettings | Select-String '^\s*java.home\s*=' | Select-Object -First 1
    if (!$javaHomeLine) { throw 'Definir JAVA_HOME apuntando al JDK 17.' }
    $env:JAVA_HOME = $javaHomeLine.ToString().Split('=',2)[1].Trim()
}
$base=Join-Path $project '.local/tomcat'
if (!(Test-Path (Join-Path $project 'target/analitiq.war'))) { throw 'Primero ejecutar mvn clean verify.' }
if (!(Test-Path (Join-Path $project '.local/analitiq.properties'))) { throw 'Falta .local/analitiq.properties. Ver README.' }
if (!(Test-Path (Join-Path $base 'conf/server.xml'))) {
    New-Item -ItemType Directory -Force $base | Out-Null
    Copy-Item -LiteralPath (Join-Path $TomcatHome 'conf') -Destination $base -Recurse
    foreach($dir in @('logs','temp','work','webapps')) { New-Item -ItemType Directory -Force (Join-Path $base $dir) | Out-Null }
    [xml]$server=Get-Content (Join-Path $base 'conf/server.xml')
    $server.Server.port='-1'
    $connector=$server.Server.Service.Connector | Where-Object {$_.protocol -eq 'HTTP/1.1'}
    $connector.port=[string]$Port
    $connector.SetAttribute('address','127.0.0.1')
    $server.Save((Join-Path $base 'conf/server.xml'))
}
Copy-Item -LiteralPath (Join-Path $project 'target/analitiq.war') -Destination (Join-Path $base 'webapps/analitiq.war') -Force
$env:CATALINA_HOME=$TomcatHome
$env:CATALINA_BASE=$base
$env:ANALITIQ_CONFIG=Join-Path $project '.local/analitiq.properties'
Write-Output "Aplicación local: http://127.0.0.1:$Port/analitiq/ (Ctrl+C para detener)."
& (Join-Path $TomcatHome 'bin/catalina.bat') run
