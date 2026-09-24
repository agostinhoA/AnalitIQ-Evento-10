param(
    [string]$MysqlExe = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe',
    [string]$HostMysql = '127.0.0.1',
    [int]$PortMysql = 3306,
    [string]$AdminUser = 'root'
)

$ErrorActionPreference = 'Stop'
$OutputEncoding = [Text.UTF8Encoding]::new($false)
$project = Split-Path $PSScriptRoot -Parent
if (!(Test-Path -LiteralPath $MysqlExe -PathType Leaf)) { throw "No se encontró mysql.exe: $MysqlExe" }

$localDir = Join-Path $project '.local'
$configPath = Join-Path $localDir 'analitiq.properties'
if (Test-Path -LiteralPath $configPath) {
    throw "Ya existe $configPath. No se modificó la configuración."
}

function New-LocalSecret {
    $bytes = New-Object byte[] 32
    $generator = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $generator.GetBytes($bytes) } finally { $generator.Dispose() }
    return [BitConverter]::ToString($bytes).Replace('-', '')
}

$secret = Read-Host "Contraseña de $AdminUser en MySQL (queda sólo en esta sesión)" -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try {
    $env:MYSQL_PWD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
}

$mysqlArgs = @('--protocol=TCP', "--host=$HostMysql", "--port=$PortMysql", "--user=$AdminUser", '--default-character-set=utf8mb4', '--batch', '--skip-column-names')
try {
    $version = & $MysqlExe @mysqlArgs '--execute=SELECT VERSION();'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo autenticar en MySQL.' }
    Write-Output "MySQL: $version"

    $existingDb = & $MysqlExe @mysqlArgs "--execute=SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'analitiq_demo';"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo comprobar el esquema.' }
    if ($existingDb) { throw 'analitiq_demo ya existe. No se importó ni modificó ningún dato.' }

    $existingUsers = & $MysqlExe @mysqlArgs "--execute=SELECT User FROM mysql.user WHERE Host = 'localhost' AND User IN ('analitiq_lectura','analitiq_registro');"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudieron comprobar las cuentas.' }
    if ($existingUsers) { throw 'Ya existe una cuenta de la aplicación. No se modificó ningún dato.' }

    foreach ($name in @('01-estructura.sql', '02-datos-prueba.sql', '04-datos-prueba-deudas.sql', '05-datos-prueba-historicos.sql', '06-evento3.sql')) {
        Get-Content -LiteralPath (Join-Path $project "database/$name") -Raw -Encoding utf8 | & $MysqlExe @mysqlArgs
        if ($LASTEXITCODE -ne 0) { throw "Falló $name. La base puede estar incompleta; revisar antes de reintentar." }
        Write-Output "Aplicado: $name"
    }

    $readPassword = New-LocalSecret
    $writePassword = New-LocalSecret
    $accountSql = @"
CREATE USER 'analitiq_lectura'@'localhost' IDENTIFIED BY '$readPassword';
GRANT SELECT ON analitiq_demo.* TO 'analitiq_lectura'@'localhost';
CREATE USER 'analitiq_registro'@'localhost' IDENTIFIED BY '$writePassword';
GRANT SELECT ON analitiq_demo.* TO 'analitiq_registro'@'localhost';
GRANT INSERT (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo,token_registro)
 ON analitiq_demo.tratamientos TO 'analitiq_registro'@'localhost';
"@
    $accountSql | & $MysqlExe @mysqlArgs
    if ($LASTEXITCODE -ne 0) { throw 'Falló la creación de cuentas. Revisar antes de reintentar.' }

    New-Item -ItemType Directory -Path $localDir -Force | Out-Null
    $configuration = @"
db.url=jdbc:mysql://${HostMysql}:${PortMysql}/analitiq_demo?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=America/Argentina/Buenos_Aires
db.user=analitiq_lectura
db.password=$readPassword
db.write.user=analitiq_registro
db.write.password=$writePassword
"@
    [IO.File]::WriteAllText($configPath, "$configuration`n", [Text.UTF8Encoding]::new($false))
    Write-Output 'Demo y cuentas preparadas. Configuración privada creada en .local/analitiq.properties.'
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    Remove-Variable secret,readPassword,writePassword,accountSql,configuration -ErrorAction SilentlyContinue
}
