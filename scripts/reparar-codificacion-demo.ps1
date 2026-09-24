param(
    [string]$MysqlExe = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
)

$ErrorActionPreference = 'Stop'
if (!(Test-Path -LiteralPath $MysqlExe -PathType Leaf)) { throw "No se encontró mysql.exe: $MysqlExe" }

$secret = Read-Host 'Contraseña de root de MySQL para reparar los textos de la demo' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try {
    $env:MYSQL_PWD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
}

try {
    $mysqlArgs = @('--protocol=TCP', '--host=127.0.0.1', '--port=3306', '--user=root', '--default-character-set=utf8mb4', '--batch', '--skip-column-names')
    $constraint = & $MysqlExe @mysqlArgs "--execute=SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA='analitiq_demo' AND TABLE_NAME='tipos_monedas' AND CONSTRAINT_TYPE='CHECK';"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo comprobar la regla de monedas.' }
    if ($constraint -eq 'tipos_monedas_chk_1') {
        $transition = "ALTER TABLE analitiq_demo.tipos_monedas DROP CHECK tipos_monedas_chk_1, ADD CONSTRAINT ck_tipos_monedas_transicion CHECK (nombre_moneda IN ('Peso Argentino','Yen','D?lar',CONVERT(0x44C3B36C6172 USING utf8mb4) COLLATE utf8mb4_0900_as_cs,'Euro'));"
        & $MysqlExe @mysqlArgs "--execute=$transition"
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar la regla de monedas.' }
        $constraint = 'ck_tipos_monedas_transicion'
    }
    if ($constraint -notin @('ck_tipos_monedas_transicion', 'ck_tipos_monedas_utf8')) {
        throw "Regla de monedas inesperada: $constraint. No se modificaron los textos."
    }

    # El SQL usa sólo ASCII: los valores correctos están codificados como bytes UTF-8.
    # Cada UPDATE exige el valor dañado exacto para no reemplazar ediciones posteriores.
    $sql = @'
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE analitiq_demo.pacientes SET apellido_paciente=CONVERT(0x50C3A972657A USING utf8mb4) WHERE dni_paciente IN ('30111222','30999888') AND apellido_paciente='P?rez';
UPDATE analitiq_demo.pacientes SET nombre_paciente=CONVERT(0x4C7563C3AD61 USING utf8mb4) WHERE dni_paciente='32444555' AND nombre_paciente='Luc?a';
UPDATE analitiq_demo.pacientes SET apellido_paciente=CONVERT(0x47C3B36D657A USING utf8mb4) WHERE dni_paciente='32444555' AND apellido_paciente='G?mez';
UPDATE analitiq_demo.pacientes SET nombre_paciente=CONVERT(0x536F66C3AD61 USING utf8mb4) WHERE dni_paciente='45000001' AND nombre_paciente='Sof?a';
UPDATE analitiq_demo.tipos_tratamientos SET descripcion_general=CONVERT(0x416C696E65616369C3B36E207920636F727265636369C3B36E206465206C61206D6F72646964612E USING utf8mb4) WHERE codigo_tipo_tratamiento=1 AND descripcion_general='Alineaci?n y correcci?n de la mordida.';
UPDATE analitiq_demo.tipos_tratamientos SET descripcion_general=CONVERT(0x5265706F73696369C3B36E20646520756E61207069657A612064656E74616C2E USING utf8mb4) WHERE codigo_tipo_tratamiento=2 AND descripcion_general='Reposici?n de una pieza dental.';
UPDATE analitiq_demo.tipos_tratamientos SET descripcion_general=CONVERT(0x54726174616D69656E746F20656E646F64C3B36E7469636F2E USING utf8mb4) WHERE codigo_tipo_tratamiento=3 AND descripcion_general='Tratamiento endod?ntico.';
UPDATE analitiq_demo.tipos_monedas SET nombre_moneda=CONVERT(0x44C3B36C6172 USING utf8mb4) WHERE codigo_tipo_moneda=3 AND nombre_moneda='D?lar';
UPDATE analitiq_demo.tratamientos SET objetivos=CONVERT(0x416C696E65616369C3B36E2064656E74616C USING utf8mb4) WHERE codigo_tratamiento=2101 AND objetivos='Alineaci?n dental';
UPDATE analitiq_demo.tratamientos SET objetivos=CONVERT(0x5265706F73696369C3B36E206465207069657A61 USING utf8mb4) WHERE codigo_tratamiento=2102 AND objetivos='Reposici?n de pieza';
COMMIT;
'@
    $sql | & $MysqlExe @mysqlArgs
    if ($LASTEXITCODE -ne 0) { throw 'Falló la reparación; comprobar el estado antes de reintentar.' }
    if ($constraint -eq 'ck_tipos_monedas_transicion') {
        $final = "ALTER TABLE analitiq_demo.tipos_monedas DROP CHECK ck_tipos_monedas_transicion, ADD CONSTRAINT ck_tipos_monedas_utf8 CHECK (nombre_moneda IN ('Peso Argentino','Yen',CONVERT(0x44C3B36C6172 USING utf8mb4) COLLATE utf8mb4_0900_as_cs,'Euro'));"
        & $MysqlExe @mysqlArgs "--execute=$final"
        if ($LASTEXITCODE -ne 0) { throw 'Los textos se corrigieron, pero falló la regla final de monedas. Reintentar este script.' }
    }
    Write-Output 'Textos de demostración reparados.'
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    Remove-Variable secret,sql,transition,final -ErrorAction SilentlyContinue
}
