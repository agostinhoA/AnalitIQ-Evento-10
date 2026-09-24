# Evento 3: registrar un tratamiento

Implementado en el proyecto Maven existente, con Java 17, Tomcat 9.0.122, JSP/JSTL, Servlets, servicios y JDBC. El inicio `/analitiq/` ahora muestra un menú con dos opciones: registrar un tratamiento y consultar el informe de deudas. El evento 10 conserva `/deudas` y el alias `/pagos`.

## Alcance y fuentes

Se aplicó el pedido adjunto y la aclaración posterior del usuario: **sin límite de tratamientos; sólo un tratamiento activo por tipo y paciente**. Por eso no se implementa el máximo de tres ni el rechazo de un cuarto por cantidad.

Se revisaron las páginas impresas D54–D57 (páginas 106–109 del archivo) de `Fase de Diseño e Integracion - Tutoria 17.9.pdf`, disponible en el proyecto. El nuevo adjunto sólo contenía texto; no estaban el PDF actualizado ni las dos imágenes mencionadas. La distribución usa los componentes y colores existentes; no se afirma haber cotejado las imágenes ausentes.

Correspondencia con los ELP:

| Proceso | Implementación |
|---|---|
| EvaluarIngresoTratamiento | Servlet recibe datos; `DatosTratamiento` valida formato y longitudes; `RegistroTratamientoService` coordina una transacción |
| ObtenerDatosPaciente | DAO busca por DNI; rechaza inexistentes sin crearlos |
| validarTratamientoPermitido | DAO comprueba catálogo y activo del mismo tipo; índice único garantiza la regla ante concurrencia |
| Generar registro | INSERT parametrizado, clave AUTO_INCREMENT devuelta por JDBC y estado SI |

Diferencias de nombres: las tablas reales son `pacientes`, `tipos_tratamientos` y `tratamientos`, en minúsculas. La columna existente es `objetivos` (TEXT), no `objetivo`; no se agregó una columna duplicada. `pronostico` también es TEXT: se valida el límite real de 65.535 bytes UTF-8. Los campos opcionales vacíos se guardan como NULL.

El selector muestra el nombre del catálogo, pero envía y persiste `codigo_tipo_tratamiento`. No crea tipos. Inicio obligatorio; finalización opcional, igual o posterior al inicio. No se restringe por fecha actual. Las fechas deben pertenecer al dominio DATE de MySQL, años 1000–9999. El DNI conserva el formato de 7–8 dígitos.

## Persistencia y concurrencia

`database/06-evento3.sql` conserva todas las filas y relaciones y realiza un único ALTER atómico:

- Agrega AUTO_INCREMENT al código existente: no usa MAX + 1 ni cambia los códigos previos.
- Agrega `tipo_activo`, columna generada técnica e invisible, y un índice único por DNI/tipo activo. Los históricos devuelven NULL y pueden repetirse. Si ya existen activos duplicados, el ALTER falla; no elimina ni corrige registros silenciosamente.
- Agrega `token_registro`, técnico, nullable, invisible y único. Identifica el formulario que originó cada alta y permite devolver el resultado original ante reenvíos, incluso desde otra instancia del servicio. Los registros anteriores quedan con NULL.

Las columnas invisibles conservan los INSERT de ocho valores de los scripts anteriores. No son datos que ingresa el odontólogo. La migración suspende temporalmente FOREIGN_KEY_CHECKS **sólo en su conexión administrativa** para evitar el error 1833 al añadir AUTO_INCREMENT a una clave referenciada; no cambia tipos ni valores. Ejecutarla en una conexión exclusiva, sin escrituras simultáneas, y cerrarla ante cualquier error. No usar `--force`. Las restricciones permanecen definidas y se comprobaron las referencias después de la migración.

La comprobación y el INSERT ocurren en una transacción READ COMMITTED; los índices únicos de InnoDB deciden las carreras entre solicitudes. No se necesita conceder UPDATE sobre pacientes para bloquearlos. Una colisión del mismo token recupera el alta anterior; una colisión del tipo activo devuelve un mensaje de negocio.

Cada sesión tiene CSRF y tokens propios, separados de las búsquedas del evento 10; máximo 30 formularios, vigencia de 30 minutos (la sesión vence a los 20 minutos de inactividad). Los tokens ajenos/vencidos se rechazan. POST exitoso devuelve 303 a la confirmación; recargar esa página no registra otra vez. Los errores de validación conservan campos y token. Los errores SQL no se muestran al usuario.

El registro sólo inserta en tratamientos. No crea presupuestos, cuotas, pagos, pacientes ni consentimientos. Un tratamiento nuevo sin cuotas no aparece como deuda.

## Configuración y SQL

**Esta computadora ya está preparada. No volver a ejecutar la migración.** Se respaldó la demo en `.local/antes-evento3-20260923-234401.sql`; la migración mantuvo sus 15 tratamientos con todos sus valores anteriores y cero referencias huérfanas. No se borró ni recreó la demo.

En otra instalación:

1. Respaldar la base. En una base nueva, importar 01, 02, 04 y opcionalmente 05. En una existente, conservar los datos y revisar que no haya activos duplicados.
2. Ejecutar una sola vez `database/06-evento3.sql`, ajustando el nombre del esquema si corresponde. Ante error detenerse y revisar; no ejecutar a ciegas nuevamente.
3. Copiar `database/07-usuario-registro.sql.example` a un archivo privado, reemplazar contraseña/host y ejecutarlo como administrador. Concede SELECT y sólo INSERT de las columnas necesarias de tratamientos. La cuenta de lectura del evento 10 permanece separada y sin escritura.
4. Añadir a la configuración externa `db.write.user` y `db.write.password`. También se admiten `ANALITIQ_DB_WRITE_USER` y `ANALITIQ_DB_WRITE_PASSWORD`. Ambos pools usan la URL externa `db.url` existente. Las credenciales reales no están en Git ni en el WAR.
5. Reiniciar o desplegar nuevamente Tomcat para cargar los pools. Si falta la cuenta de escritura, el evento 3 devuelve indisponibilidad; el evento 10 puede seguir funcionando con su cuenta de lectura.

La aplicación no migra automáticamente al arrancar. El usuario de registro no puede actualizar/borrar tratamientos, registrar pacientes ni escribir pagos. Cada pool permite hasta diez conexiones; considerar hasta veinte al dimensionar MySQL.

## Ejecutar y probar manualmente

En NetBeans: abrir la carpeta del proyecto, JDK 17, servidor Tomcat 9, contexto `/analitiq`; cambiar Properties → Run → Relative URL a `/inicio` para iniciar por el menú. Cargar `ANALITIQ_CONFIG` en el entorno de Tomcat como indica el README; ejecutar Clean and Build y Run. No iniciar simultáneamente NetBeans y el Tomcat portable en 8080.

Menú: `http://127.0.0.1:8080/analitiq/`. Formulario: `/analitiq/tratamientos/registrar`. Se conserva el script `ejecutar-tomcat.ps1`; sin argumentos nuevos sigue usando la demo en 8080.

Casos disponibles en la demo antes de registrar nuevas altas:

| Caso | Entrada |
|---|---|
| Alta con obligatorios | DNI 30999888, Ortodoncia, inicio 23/09/2026, opcionales vacíos |
| Alta con opcionales | DNI 35666777, Implante, inicio 23/09/2026, fin 23/03/2027, objetivos y pronóstico libres |
| Histórico del mismo tipo | DNI 32444555, Conducto; el tratamiento anterior está inactivo |
| Activo duplicado | DNI 30111222, Ortodoncia; debe rechazar |
| Paciente inexistente | DNI 99999999; mensaje específico |
| Fechas invertidas | Inicio 23/09/2026, fin 22/09/2026; debe conservar los datos y rechazar |
| Reenvío | Después de un alta, recargar la confirmación o reenviar el mismo POST: no agrega otra fila |
| Evento 10 | Consultar el paciente recién registrado: el nuevo tratamiento sin cuotas no crea deudas |

Las altas manuales persisten: repetir un caso exitoso con otro formulario del mismo tipo produce el rechazo por activo duplicado. Las pruebas automáticas de escritura usan otra base para conservar estos ejemplos disponibles.

## Pruebas automáticas

`mvn clean verify` ejecuta las pruebas unitarias; las de MySQL requieren variables explícitas. Para la suite del evento 3 preparar **exclusivamente** `analitiq_evento3_test`: copiar 01/02/04/05/06 a archivos temporales reemplazando `analitiq_demo` por ese nombre e importarlos en orden. La creación debe fallar si la base ya existe; no sobrescribirla. Conceder a la cuenta de registro los mismos permisos del ejemplo 07 sobre ese esquema. Crear `.local/evento3-test.properties` con URL de ese esquema y credenciales de registro en `db.user/password` y `db.write.user/password`.

```powershell
$env:ANALITIQ_CONFIG = (Resolve-Path '.local/analitiq.properties').Path
$env:ANALITIQ_TEST_DB = '1'
$env:ANALITIQ_TEST_DEUDAS = '1'
$env:ANALITIQ_REGISTRO_TEST_CONFIG = (Resolve-Path '.local/evento3-test.properties').Path
$env:ANALITIQ_TEST_ADMIN_USER = 'USUARIO_ADMIN_DE_PRUEBAS'
# Si corresponde, cargar ANALITIQ_TEST_ADMIN_PASSWORD privadamente, sin guardarla en Git.
mvn clean verify
```

Los tests Java comprueban el nombre exacto del esquema antes de escribir. La cuenta administrativa sólo prepara y limpia pacientes ficticios reservados 88000001–88000009; las altas se realizan con la cuenta restringida. Nunca apuntar las pruebas de escritura a la demo ni a datos clínicos.

Para HTTP, iniciar otro Tomcat que use la configuración de pruebas:

```powershell
./scripts/ejecutar-tomcat.ps1 -TomcatHome .tools/apache-tomcat-9.0.122 -Port 8081 -BaseDirectory .local/tomcat-evento3-test -ConfigFile .local/evento3-test.properties
```

En otra terminal:

```powershell
$env:ANALITIQ_TEST_MYSQL_EXE = 'RUTA_A_MYSQL/bin/mysql.exe'
$env:ANALITIQ_TEST_ADMIN_USER = 'USUARIO_ADMIN_DE_PRUEBAS'
python scripts/probar-registro-http.py http://127.0.0.1:8081/analitiq
python scripts/probar-http.py http://127.0.0.1:8080/analitiq
```

La primera suite crea y limpia fixtures 88100001–88100009 sólo en el esquema aislado. La segunda no escribe y comprueba el evento 10 en la demo. Los scripts `BaseDirectory/ConfigFile` son opcionales: permiten separar ambas instancias sin alterar la configuración principal.

## Archivos

- `model/TipoTratamiento` y `TratamientoRegistrado`: catálogo y confirmación.
- `service/DatosTratamiento`, `RegistroTratamientoService` y `RegistroSesion`: validaciones, transacción e identificación de formularios.
- `dao/RegistroTratamientoDao`: consultas parametrizadas y alta.
- `web/InicioServlet` y `RegistroTratamientoServlet`: navegación, formularios, errores y redirección.
- `config/Database` y `AppListener`: pool de escritura separado del de lectura.
- JSP de inicio, registro y confirmación; header/error compartidos, theme.css y welcome-file: menú y presentación.
- Migración 06, ejemplo 07 y configuración externa: instalación sin recrear datos.
- Pruebas Java y `probar-registro-http.py`: validaciones, persistencia, permisos y concurrencia; `probar-http.py` adapta sólo la comprobación de la página de inicio al menú.

Las rutas Java parten de `src/main/java/ar/com/analitiq`; las JSP, de `src/main/webapp/WEB-INF/views`. El WAR se genera en `target/analitiq.war`. Para publicar, seguir el README y aplicar 06/07 antes de desplegar; usar datos ficticios. El alta no agrega autenticación: el acceso con datos reales requiere un alcance de seguridad adicional.
