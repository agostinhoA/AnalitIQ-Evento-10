# Recarga de datos y filtro de tratamiento

El 23/09/2026, por pedido explícito del usuario, se respaldaron y borraron los registros de las 13 tablas de `analitiq_demo`, y se cargaron nuevamente los datos ficticios de los scripts 02, 04 y 05. El borrado y la carga se ejecutaron en una sola transacción, respetando las claves foráneas. Se conservaron el esquema, las cuentas, los permisos y las demás bases de datos.

Respaldo privado anterior: `.local/antes-recarga-demo-20260923-223454.sql`. Está excluido de Git y del WAR. No es necesario ejecutar nuevamente la recarga en esta computadora.

La demo contiene 10 pacientes, 15 tratamientos y 14 cuotas. La auditoría por paciente y tipo no encontró más de un tratamiento activo del mismo tipo. Eva y Nora eran casos deliberadamente inconsistentes del antiguo script 05; ahora tienen un activo por tipo y tratamientos históricos inactivos. El nuevo archivo `database/05-datos-prueba-historicos.sql` reemplaza al de inconsistencias. Las pruebas unitarias conservan casos inválidos sintéticos para verificar los avisos, sin introducirlos en MySQL.

El filtro de tratamiento ahora compara el nombre completo sin distinguir mayúsculas y minúsculas mediante SQL parametrizado. No cambia el criterio de búsqueda por nombre/apellido del paciente. Los nombres inexistentes siguen produciendo un mensaje sin cuotas y HTTP 200.

## Verificado

- Maven `clean verify`: 21 pruebas Java aprobadas, cero fallos, errores u omitidas, con las pruebas de MySQL habilitadas.
- Tomcat: 21 pruebas HTTP aprobadas sobre las JSP y la base reales, incluyendo `ortodoncia`, `Ortodoncia`, `ORTODONCIA`, `implante` y conservación del filtro durante la selección.
- WAR generado y desplegado con SHA-256 idéntico: `477A73A779C5AEA2B46635510AF4CDC02480D84272D8194FACD1A1B69FE89468`.
- Registros locales: `tmp/recarga-build.log` y `tmp/recarga-http.log`.

## Caso para probar

Abrir una búsqueda nueva en `/analitiq/deudas`. DNI `45000001`, Desde `01/09/2026`, Hasta `15/09/2026`, Tratamiento `ortodoncia`: aparecen dos cuotas pendientes de Sofía Molina, por un total de `20.000,00`. Repetir con `Ortodoncia` o `ORTODONCIA` da el mismo resultado. Eva (`45000003`) y Nora (`45000004`) muestran ausencia de cuotas, sin inconsistencias.

Se compiló y desplegó localmente. No se realizó una publicación en internet ni una migración a otro servidor.
