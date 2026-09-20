param(
    [Parameter(Mandatory=$true)][string]$MysqlExe,
    [string]$HostMysql = '127.0.0.1', [int]$PortMysql = 3306,
    [string]$AdminUser = 'root', [switch]$SolicitarClave
)
$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$mysqlArgs = @('--protocol=TCP',"--host=$HostMysql","--port=$PortMysql","--user=$AdminUser",'--default-character-set=utf8mb4','--batch','--skip-column-names')
if ($SolicitarClave) { $mysqlArgs += '-p' }
$existing = & $MysqlExe @mysqlArgs '--execute=SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = ''analitiq_demo'';'
if ($LASTEXITCODE -ne 0) { throw 'No se pudo conectar al administrador de MySQL.' }
if ($existing) { throw 'analitiq_demo ya existe. No se modificó. Revisar el README para una instalación existente.' }
$existingUser = & $MysqlExe @mysqlArgs '--execute=SELECT User FROM mysql.user WHERE User = ''analitiq_lectura'' AND Host = ''localhost'';'
if ($LASTEXITCODE -ne 0 -or $existingUser) { throw 'El usuario ya existe o no se pudo comprobar. No se modificó.' }
foreach ($script in @('01-estructura.sql','02-datos-prueba.sql')) {
    Get-Content -LiteralPath (Join-Path $project "database/$script") -Raw -Encoding utf8 | & $MysqlExe @mysqlArgs
    if ($LASTEXITCODE -ne 0) { throw "Falló $script. Revisar el estado de la base; no volver a ejecutar a ciegas." }
}
$secret = [Convert]::ToHexString([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$userSql = "CREATE USER 'analitiq_lectura'@'localhost' IDENTIFIED BY '$secret'; GRANT SELECT ON analitiq_demo.* TO 'analitiq_lectura'@'localhost';"
$userSql | & $MysqlExe @mysqlArgs
if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear el usuario de lectura. La base permanece intacta.' }
$localDir = Join-Path $project '.local'
New-Item -ItemType Directory -Force $localDir | Out-Null
$configText = "db.url=jdbc:mysql://${HostMysql}:${PortMysql}/analitiq_demo?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=America/Argentina/Buenos_Aires`ndb.user=analitiq_lectura`ndb.password=$secret`n"
[IO.File]::WriteAllText((Join-Path $localDir 'analitiq.properties'),$configText,[Text.UTF8Encoding]::new($false))
Write-Output 'Base de demostración creada. Usuario de sólo lectura configurado. Credenciales locales en .local/analitiq.properties (excluidas de Git y del WAR).'
