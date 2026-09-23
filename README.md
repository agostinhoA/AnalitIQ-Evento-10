# AnalitIQ · Ver informe de deudas de un paciente

Proyecto Java web Maven, JSP/JSTL/EL, Servlets y JDBC. Implementa la especificación del **23/09/2026**, que tiene prioridad sobre el alcance anterior del coloquio y el TP. Conserva arquitectura, dependencias, Tomcat y configuración externa. El [rediseño visual](docs/rediseno-visual.md) usa blanco, gris oscuro y rojo, con navegación superior y adaptación a móvil.

Se muestran únicamente cuotas en estado **Adeuda**, de tratamientos con **activo = SI**, cuya fecha de vencimiento esté dentro de **Desde/Hasta, ambos incluidos**. Ambas fechas son obligatorias. **No hay corte por hoy**: una cuota futura se incluye si está dentro del rango y sigue en Adeuda.

El formulario admite DNI, nombre y apellido juntos, o identificación vacía para elegir de la lista completa. DNI y nombre/apellido simultáneos se rechazan. La lista completa exige selección incluso si contiene un único paciente; una búsqueda con criterio y una sola coincidencia genera el informe directamente. El nombre de tratamiento es opcional y se compara exactamente con el catálogo.

El informe identifica una vez al paciente y al rango; muestra un bloque por tratamiento con cuotas coincidentes y las columnas Cuota, Fecha de vencimiento y Saldo pendiente. Cada bloque incluye cantidad y suma exacta de sus filas con BigDecimal. No hay bloques vacíos, importes parciales ni operaciones de pago. Los estados se consultan por su relación y nombre, sin fijar IDs numéricos.

Ruta principal: `/deudas`; `/pagos` abre el mismo evento corregido. Detalles y archivos modificados: [especificación implementada](docs/evento10-2026-09-23.md). Las imágenes mencionadas en el pedido no estaban adjuntas; se conservó el estilo existente. El TP y los documentos externos no fueron modificados.

## Inicio en esta computadora

La preparación realizada creó una base nueva `analitiq_demo`, un usuario `analitiq_lectura` con permiso **SELECT** y un archivo privado `.local/analitiq.properties`. Las bases anteriores se conservaron. **No vuelvas a importar los scripts sobre esta base.**

El servidor portable de pruebas está en `.tools/apache-tomcat-9.0.122`. No se modificó el Tomcat de XAMPP. Desde la raíz del proyecto, en PowerShell 7:

```powershell
./scripts/ejecutar-tomcat.ps1 -TomcatHome .tools/apache-tomcat-9.0.122
```

Abrir [AnalitIQ local](http://127.0.0.1:8080/analitiq/). El servidor debe estar ejecutándose y MySQL de WAMP encendido. Ctrl+C detiene el servidor. El script usa una instancia propia en `.local/tomcat` y escucha sólo en la interfaz local. No ejecutes simultáneamente este script y NetBeans sobre el mismo puerto/instancia.

## Entorno comprobado y compatibilidad

| Componente | Detectado / elegido |
|---|---|
| Java | Oracle JDK 17.0.12 instalado; compilación para Java 17 |
| NetBeans | 19 instalado |
| Maven | 3.9.3 incluido en NetBeans; no estaba en PATH |
| MySQL | WAMP MySQL 8.4.7 activo, TCP 3306, verificado con `SELECT VERSION()` |
| Otros motores | MariaDB 11.4.9 de WAMP; MySQL 9.5 instalado pero detenido. No se utilizaron |
| Servidor existente | XAMPP Tomcat 8.5.96. No se usa para este proyecto |
| Servidor del proyecto | Tomcat 9.0.122 portable, descarga oficial con SHA-512 verificado |
| APIs | Servlet 4.0, JSP 2.3, `javax.servlet`, JSTL 1.2 |
| JDBC | MySQL Connector/J 9.7.0, compatible con MySQL 8.0+ y Java 8+ |
| Conexiones | HikariCP 5.1.0, máximo 10 conexiones, sólo lectura |

[Tomcat 9](https://tomcat.apache.org/whichversion.html) implementa estas APIs y funciona con Java 17. Tomcat 10/11 cambia a `jakarta.servlet` y **no es un sustituto directo** para este WAR. El Apache HTTP Server de WAMP **no ejecuta JSP**: WAMP provee MySQL; Tomcat ejecuta la aplicación Java. Ver también la [compatibilidad de Connector/J 9.7](https://dev.mysql.com/doc/relnotes/connector-j/en/news-9-7-0.html).

## Abrir en NetBeans

1. File → Open Project. Seleccionar esta carpeta, que contiene `pom.xml`.
2. Verificar JDK 17 en Tools → Java Platforms. El proyecto fija `maven.compiler.release=17`.
3. Si no aparece soporte web/Tomcat, activar Java Web and EE desde Tools → Plugins.
4. Services → Servers → Add Server → Apache Tomcat or TomEE. Seleccionar una distribución **Tomcat 9.0.x** (puede usarse la carpeta `.tools/apache-tomcat-9.0.122` disponible). Para otra máquina, descargar el ZIP Core de la [página oficial](https://tomcat.apache.org/download-90.cgi), verificar SHA-512 y descomprimir fuera del proyecto o en una carpeta local ignorada.
5. Registrar el JDK 17 y configurar el usuario de despliegue que solicite NetBeans en ese Tomcat. Ese usuario de Tomcat es distinto del usuario MySQL. Si la integración requiere Manager, habilitar sólo los roles que solicita NetBeans (`manager-script` para despliegue automático) y mantener Manager limitado a la máquina local. No incluir esas credenciales en el proyecto.
6. Properties del proyecto → Run: elegir Tomcat 9, contexto `/analitiq`, URL relativa `/deudas`.
7. Antes de iniciar Tomcat, configurar el archivo externo como se explica abajo. Clean and Build genera `target/analitiq.war`. Run despliega el proyecto. La alternativa sin integración de Manager es copiar el WAR a `webapps` y ejecutar el script local.

NetBeans puede usar su Maven incluido. Desde una terminal con Maven en PATH:

```powershell
mvn clean verify
```

Si no está en PATH, definir `$maven` con la ubicación de `mvn.cmd` del Maven incluido en NetBeans y ejecutar `& $maven clean verify`. En esta entrega la caché usada para compilar está en `.tools/m2`; para reutilizarla: `& $maven '-Dmaven.repo.local=.tools/m2' clean verify`. La primera compilación requiere internet. No se necesitan React ni Spring Boot.

## Preparar MySQL en otra instalación

Arrancar **MySQL** desde WAMP (no confundirlo con MariaDB). Confirmar versión y puerto con el cliente o Workbench. La estructura está preparada para MySQL 8.4 o posterior.

Los archivos están separados:

- `database/01-estructura.sql`: base y 13 tablas con claves y relaciones.
- `database/02-datos-prueba.sql`: catálogos y datos ficticios, dentro de una transacción.
- `database/03-usuario.sql.example`: ejemplo de usuario de aplicación y permiso SELECT.
- `database/04-datos-prueba-deudas.sql`: casos nuevos para el evento corregido.
- `database/05-datos-prueba-inconsistencias.sql`: casos opcionales para probar los avisos.

El preparador automatizado crea la demo original (01/02); luego importar 04 y, si se desean esos casos, 05. No se necesita cambiar el esquema ni los permisos del usuario existente.

### Opción automatizada en Windows

En PowerShell 7, especificar la ruta real de `mysql.exe` (no de `mysqld.exe`):

```powershell
./scripts/preparar-demo.ps1 -MysqlExe 'RUTA_A_MYSQL/bin/mysql.exe' -SolicitarClave
```

El script solicita la clave de administrador mediante el cliente MySQL; no la guarda. Si el administrador local no tiene contraseña, omitir `-SolicitarClave`. Por defecto usa 127.0.0.1:3306 y root exclusivamente para preparar la base; se pueden indicar `-HostMysql`, `-PortMysql` y `-AdminUser`. Genera una contraseña aleatoria para la aplicación y la escribe en `.local/analitiq.properties`, sin mostrarla. El usuario creado usa host `localhost`: esta automatización está destinada a una aplicación y MySQL en la misma máquina. Para servidores separados seguir la instalación manual con host restringido.

Si la base o el usuario ya existen, el script **se detiene sin modificarlos**. No borra, no reemplaza, no aplica `INSERT IGNORE`, no actualiza registros existentes. Ante una importación incompleta, revisar el estado con el administrador antes de continuar. No usar `--force`.

### Opción manual

Desde un cliente MySQL autenticado como administrador:

```sql
SOURCE database/01-estructura.sql;
SOURCE database/02-datos-prueba.sql;
```

Ejecutar desde la raíz del proyecto o indicar las rutas adecuadas al cliente. `CREATE DATABASE` falla si el nombre existe: **ante ese error, detenerse y no ejecutar los siguientes archivos**. No importar la demo en una base clínica existente.

Copiar `03-usuario.sql.example` a un archivo privado fuera del repositorio, sustituir contraseña y host, y ejecutarlo. El usuario de aplicación sólo debe tener SELECT sobre las tablas del evento. Para una base existente, comparar tablas, columnas, caso, colación y relaciones antes de configurar su URL; no hay migración automática ni sustitución silenciosa de esquemas.

## Configurar la conexión

Copiar `config/analitiq.properties.example` a un archivo **fuera del WAR**, reemplazar URL, usuario y contraseña. En local puede usarse `.local/analitiq.properties` (ya generado durante esta preparación). No subir `.local` a Git, no compartir ese archivo ni empaquetarlo para publicar.

La carga ocurre al iniciar la aplicación:

1. Archivo indicado por `-Danalitiq.config=RUTA` o, si no se especifica esa propiedad Java, por `ANALITIQ_CONFIG`.
2. Las variables `ANALITIQ_DB_URL`, `ANALITIQ_DB_USER` y `ANALITIQ_DB_PASSWORD` sobrescriben sus valores del archivo. También permiten configurar todo sin archivo.

Ejemplo PowerShell antes de iniciar Tomcat o NetBeans desde esa terminal:

```powershell
$env:ANALITIQ_CONFIG = (Resolve-Path '.local/analitiq.properties').Path
```

NetBeans ya abierto no hereda variables nuevas. Reiniciarlo desde esa terminal, o agregar en las opciones JVM de **Tomcat** `-Danalitiq.config="RUTA_AL_ARCHIVO_PRIVADO"` (no en las opciones de Maven). Otra alternativa es definir `ANALITIQ_CONFIG` en `CATALINA_BASE/bin/setenv.bat` con `set "ANALITIQ_CONFIG=RUTA_AL_ARCHIVO_PRIVADO"`.

Una ruta absoluta al archivo privado es una configuración **del entorno**, no forma parte del código ni del WAR. Cambios de configuración requieren reiniciar/re desplegar la aplicación. Si la configuración falta o MySQL no está disponible, se devuelve un mensaje de indisponibilidad y HTTP 503; no se muestran contraseñas ni trazas técnicas al usuario.

La URL de ejemplo con `sslMode=DISABLED&allowPublicKeyRetrieval=true` es sólo para MySQL en la máquina local. Para una conexión remota usar TLS validado (`sslMode=VERIFY_IDENTITY`), certificado confiable y el hostname del servidor. Mantener la zona horaria elegida o acordar otra para el despliegue.

## Ejecutar el WAR en Tomcat

1. Compilar con `mvn clean verify`.
2. Copiar `target/analitiq.war` a `CATALINA_BASE/webapps/analitiq.war` con Tomcat detenido, o desplegar con NetBeans.
3. Definir configuración externa y JDK 17; iniciar `bin/catalina.bat run` (Windows) o `bin/catalina.sh run` (Linux).
4. Abrir `http://127.0.0.1:8080/analitiq/`. Si el puerto cambia, usar el real.

Todos los formularios, estilos y enlaces internos respetan el context path; no contienen enlaces a localhost. El WAR también puede renombrarse para otro contexto. La URL local anterior es sólo una instrucción de prueba.

El script `ejecutar-tomcat.ps1` crea su configuración al primer arranque. `-Port` se aplica en ese primer arranque; después, editar el conector de `.local/tomcat/conf/server.xml` con Tomcat detenido para cambiarlo. No publicar esa instancia local directamente.

## Regla de tratamientos

Un paciente puede tener cualquier cantidad de tratamientos registrados, incluidos históricos del mismo tipo. Sólo se admite un tratamiento **activo por tipo** para ese paciente. No se impone un máximo de tres: los tres tipos actuales del catálogo no son un límite de cantidad de tratamientos. El informe advierte duplicados activos sin modificar registros.

## Datos y recorridos para probar

**En esta computadora no hay que ejecutar SQL ni reimportar datos.** Se reutiliza la demo existente, con los scripts 01/02/04/05 ya importados. Este cambio no modifica el esquema ni inserta/actualiza registros. En una instalación nueva, seguir la preparación anterior; 05 es opcional salvo para ejecutar todas las pruebas de integración.

Búsqueda de nombres/apellidos y tratamiento exacta, incluyendo mayúsculas y tildes. Valores existentes del catálogo: `Ortodoncia`, `Implante`, `Conducto`. Un tratamiento inexistente devuelve un informe sin cuotas, no una lista de pacientes.

Salvo indicación distinta, usar **Desde 01/01/2026, Hasta 31/12/2027**, tratamiento vacío:

| Entrada | Resultado esperado |
|---|---|
| DNI `30111222` | Juan Pérez, Ortodoncia: cuota 3 (603), cantidad 1, total 30.000,00; cuotas pagadas y consultas excluidas |
| Nombre `Lucía`, apellido `Gómez` | Coincidencia única; informe directo con identificación/rango y mensaje sin cuotas |
| Nombre `Juan`, apellido `Pérez` | Dos coincidencias, DNI 30111222 y 30999888; se exige seleccionar |
| DNI, nombre y apellido vacíos | Lista de diez pacientes, cada uno con su DNI y botón Seleccionar; aún no se genera informe |
| DNI `45000001` | Sofía Molina: Ortodoncia con 5 cuotas, total 50.000,00; Implante con 1 cuota, total 20.000,00. No aparece el Conducto sin cuotas |
| DNI `45000001`, tratamiento `Implante` | Sólo Implante, cuota 2 (2308), vence 01/12/2026, total 20.000,00; aparece aunque esa fecha sea posterior a hoy |
| DNI `45000001`, tratamiento `Conducto` o `No existe` | Identificación, rango y “No se encontraron cuotas pendientes para los criterios seleccionados”; ningún bloque |
| DNI `45000001`, Desde 01/09/2026, Hasta 15/09/2026 | Ortodoncia, cuotas 2 y 3 (2302/2303), cantidad 2, total 20.000,00; ambas fechas límite incluidas |
| DNI `45000001`, Desde/Hasta 15/09/2026 | Sólo cuota 3 (2303), cantidad 1, total 10.000,00 |
| DNI `45000002`, `37888999`, `40123456` o `30999888` | Informe sin cuotas; tratamientos inactivos/sin cuotas no crean bloques vacíos |
| DNI `45000003` o `45000004` | Avisos sólo por tipos activos repetidos (datos ficticios de 05); no existe límite numérico de tratamientos |
| DNI `99999999` o `Nadie` / `Inexistente` | “No se encontraron pacientes”; permite corregir los campos y no muestra el informe anterior |
| DNI junto con nombre/apellido; sólo nombre; sólo apellido | HTTP 400 y explicación; no se interpreta como lista completa |
| Fechas vacías, inválidas o Desde posterior a Hasta | HTTP 400; no se ejecuta una consulta con rango inválido |
| Selección de homónimos alterada para enviar DNI `45000001` | HTTP 400; sólo se permiten los pacientes recuperados por esa búsqueda |

Tratamiento y fechas se guardan en el servidor durante la selección. Cambiar los campos ocultos o enviar otros filtros en esa solicitud no los reemplaza. “Modificar filtros” → “Aplicar filtro” conserva el paciente y crea otro flujo, sin alterar informes abiertos en otras pestañas. Un filtro inválido muestra un error y conserva el informe anterior, identificado con sus criterios. “Cambiar paciente” o “Iniciar otra búsqueda” abre un formulario nuevo y no reutiliza la selección previa. Cada flujo vence a los 15 minutos; la sesión a los 20 de inactividad.

**Moneda:** ni cuotas ni presupuestos tienen un campo de moneda; sólo los pagos lo tienen. Los totales son sumas de importes registrados por tratamiento, sin símbolo monetario ni conversiones. No se infiere la moneda de pagos excluidos del informe ni se suman pagos en distintas monedas. El modelo no permite certificar una totalización multimoneda: antes de incorporar esa posibilidad se necesita definir moneda de presupuesto/cuota y una migración explícita.

## Pruebas reproducibles

`mvn clean verify` ejecuta las pruebas unitarias. Para todas las pruebas JDBC, usando la demo con scripts 01/02/04/05:

```powershell
$env:ANALITIQ_CONFIG = (Resolve-Path '.local/analitiq.properties').Path
$env:ANALITIQ_TEST_DB = '1'
$env:ANALITIQ_TEST_DEUDAS = '1'
mvn clean verify
```

Si Maven no está en PATH, usar su ruta indicada en “Abrir en NetBeans”. Se usa MySQL real y el usuario de lectura. Se prueban selección explícita en lista de un único paciente, filtros, inclusividad, cuotas futuras, estados, agrupación, cantidades, totales decimales e inconsistencias. Permanece la prueba de regresión del servicio anterior y la comprobación de rechazo de UPDATE.

Con Tomcat iniciado y Python 3:

```powershell
python scripts/probar-http.py http://127.0.0.1:8080/analitiq
```

La suite prueba formularios/JSP/Servlet/MySQL reales, validación, lista completa, homónimos, conservación de filtros, ausencia de datos anteriores, tratamiento opcional, exclusiones, fechas, totales/cantidades, sesiones, CSRF, escape HTML y concurrencia. Los resultados son independientes del día actual; los datos ficticios tienen fechas de 2026.

Resultados: **21 pruebas Java y 20 HTTP aprobadas** el 23/09/2026. Reportes en `target/surefire-reports` y detalles en [verificación](docs/verificacion.md).

## Estructura y mantenimiento

```text
src/main/java/ar/com/analitiq/
  config/    configuración externa, pool e inicio/cierre
  model/     modelos de presentación Java
  dao/       SQL parametrizado y mapeo JDBC
  service/   validación, lectura de informes y flujo por sesión
  web/       servlet y filtro HTTP
src/main/webapp/
  WEB-INF/views/  JSP no accesibles directamente
  WEB-INF/tags/   presentación de pagos conservada para otros eventos
  assets/        estilos locales, sin CDN
database/        estructura, datos y ejemplo de usuario separados
config/          ejemplo sin credenciales reales
scripts/         preparación, ejecución y pruebas
docs/            decisiones y verificación
target/analitiq.war  artefacto generado
```

Las JSP no contienen SQL ni scriptlets. Sólo presentan modelos con JSTL/EL; los datos se escapan con `c:out`. El servlet maneja peticiones HTTP sucesivas y redirecciones 303. La aplicación usa consultas parametrizadas, cierre de recursos, pool de conexiones y snapshots de lectura. Sólo el administrador de instalación escribe la base. No hay datos ficticios incorporados en listas del código de producción.

## Preparar la publicación y migrar la base

1. Elegir un servidor con JDK 17+, Tomcat 9 actualizado y MySQL 8.4+. Un hosting sólo PHP/Apache no puede ejecutar este WAR. No se ha contratado ni publicado un hosting.
2. Para una demo nueva, importar los scripts de estructura y datos ficticios en un esquema nuevo. Crear el usuario de lectura restringido al host donde corre Tomcat; no usar root ni abrir MySQL a todo internet.
3. Para trasladar la demo con datos existentes, usar un respaldo consistente, por ejemplo `mysqldump -h HOST_ORIGEN -u USUARIO_RESPALDO -p --single-transaction --no-tablespaces --set-gtid-purged=OFF --result-file=analitiq-demo.sql analitiq_demo`. No exportar el esquema `mysql` ni cuentas/credenciales. Crear manualmente un esquema **nuevo y vacío** en destino con utf8mb4 y la colación acordada; importar el dump allí. No ejecutar el seed después del dump ni sobrescribir una base existente. Recrear el usuario de lectura por separado.
4. Configurar la URL remota y secretos fuera del WAR, con TLS. Restringir lectura del archivo al usuario que ejecuta Tomcat; un gestor de secretos puede inyectar variables de entorno. No copiar `.local`, `.tools`, dumps privados o credenciales del equipo al servidor público.
5. Desplegar el WAR. Configurar HTTPS en el frontal y el dominio público; si se usa proxy, preservar el contexto `/analitiq` y configurar correctamente el conector/Tomcat para reconocer HTTPS. Configurar cookie de sesión `Secure` en producción y `SameSite=Lax` en `CookieProcessor` de Tomcat. `HttpOnly` y sesiones sólo por cookie ya están definidos.
6. Repetir las pruebas HTTP cambiando la URL base al dominio público. Validar reinicio, logs, codificación, conectividad JDBC/TLS, copias de respaldo y restore en el destino. Los enlaces internos usan el contexto de la aplicación, por lo que no necesitan cambiarse para otro host.
7. Las pruebas públicas deben contener **sólo datos ficticios**. El evento no incluye login ni autorización del odontólogo: antes de incorporar datos reales se debe acordar ese alcance e implementarlo. El WAR actual permite consultar la demo a cualquier visitante.

Para varios usuarios en una instancia, cada sesión conserva su propio flujo. Para varias instancias detrás de un balanceador, configurar afinidad de sesión o almacenamiento de sesiones compartido. El pool debe dimensionarse con el límite de conexiones del MySQL de destino. Las consultas jerárquicas están pensadas para el volumen inicial; antes de grandes historiales medir tiempos y evaluar carga agrupada/paginación sin cambiar el significado del evento.
