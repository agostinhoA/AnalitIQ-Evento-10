# AnalitIQ · Consulta de deudas

Proyecto Java web Maven, JSP/JSTL/EL, Servlets y JDBC. El evento 10 corregido permite consultar exclusivamente deudas de tratamientos activos de un paciente. Conserva las tecnologías y el estilo visual originales. No registra ni modifica pagos.

Una cuota se muestra cuando está en estado **Adeuda**, pertenece a un tratamiento con **activo = SI**, vence dentro de **Desde/Hasta (ambos incluidos)** y su vencimiento es **hoy o anterior**. Las dos fechas son obligatorias. Una cuota futura no se muestra aunque esté impaga y dentro del rango. El día actual se calcula en Argentina y aparece en el informe.

Las deudas se agrupan por tratamiento y presupuesto. Se muestra el importe completo de cada cuota; no se calculan importes parciales. Se informa cuando un paciente tiene más de tres tratamientos activos o tipos activos repetidos, sin ocultarlos ni corregir los datos.

Ruta principal: `/deudas`. La ruta previa `/pagos` abre el mismo evento corregido para conservar enlaces anteriores; ya no permite elegir consultas, tratamientos, ambos ni estados.

Detalles del cambio, archivos y ajustes pendientes del evento/DFD/ELP/diccionario: [coloquio del 17/09/2026](docs/coloquio-2026-09-17.md). Las interfaces de Lucid, página 7, no estaban disponibles; no se modificaron documentos externos.

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

## Datos y recorridos para probar

Los scripts 04 y 05 ya se importaron en esta computadora. No repetirlos. En otra base nueva, importar primero estructura y datos originales (01/02), después `04-datos-prueba-deudas.sql`. El archivo `05-datos-prueba-inconsistencias.sql` agrega casos deliberadamente inválidos y es opcional salvo para ejecutar toda la suite de integración. Ambos sólo agregan filas nuevas; no reemplazan ni actualizan registros anteriores.

La búsqueda por nombre y apellido sigue siendo exacta, distingue tildes y mayúsculas. Salvo donde se indique, usar **Desde 01/09/2026, Hasta 15/09/2026**:

| Entrada | Resultado esperado |
|---|---|
| DNI `30111222` | Juan Pérez; ortodoncia 101, presupuesto 401 y cuota adeudada 603 por 30.000. Las cuotas pagadas 601/602 y los pagos de consultas están excluidos |
| Nombre `Juan`, apellido `Pérez` | Dos resultados con DNI 30111222 y 30999888. Al seleccionar se conserva el rango guardado en el servidor |
| DNI `30999888` o `35666777` | Sin tratamientos activos |
| Nombre `Lucía`, apellido `Gómez` | Coincidencia única, acceso directo, sin tratamientos activos; el tratamiento histórico está excluido |
| DNI `45000001` | Sofía Molina: tres bloques activos (Ortodoncia, Implante, Conducto). Sólo Ortodoncia tiene cuotas coincidentes (2302 y 2303). Los otros bloques muestran el mensaje sin deudas |
| DNI `45000002` | Mateo Vidal: sólo un tratamiento inactivo, por lo que no hay tratamientos activos aunque tenga una cuota impaga |
| DNI `37888999` o `40123456` | Un bloque activo sin deudas en el rango; no se confunde con ausencia de tratamientos |
| DNI `45000003` | Eva Ramos: aviso de tipos activos repetidos y ambos tratamientos visibles (script 05) |
| DNI `45000004` | Nora Gil: aviso de máximo tres y de tipo repetido; los cuatro tratamientos siguen visibles (script 05) |
| DNI `99999999` o `Nadie` / `Inexistente` | Sin coincidencias; permite otra búsqueda y conserva las fechas |
| Fechas vacías, inválidas o Desde posterior a Hasta | Validación del servidor y HTTP 400 |
| Selección de Juan manipulada para enviar `45000001` | HTTP 400; no permite acceder a Sofía |

Para probar los límites, buscar `45000001` del **01/09/2026 al 15/09/2026**: aparecen las cuotas de ambas fechas. Cambiar a **15/09/2026–15/09/2026**: aparece sólo la cuota 2303. Aplicar **31/08/2026–31/08/2026**: aparece sólo la 2301. Las pagadas siguen excluidas.

Si hoy es 18/09/2026, la cuota 2304 (vence el 30/09) todavía no aparece incluso con Hasta 30/09. El 30/09 se incluirá como deuda de ese día si sigue en estado Adeuda. Las pruebas JDBC controlan el reloj para comprobar ese comportamiento de forma reproducible, sin cambiar la fecha de la computadora.

«Aplicar filtro» conserva el paciente validado y crea un flujo nuevo, de modo que un informe abierto en otra pestaña conserva su rango. Si el rango es inválido, se muestra el error y se conserva el informe anterior identificado con su rango aplicado. «Cambiar paciente» abre una búsqueda nueva. Cada flujo vence a los 15 minutos y la sesión a los 20 de inactividad.

Los importes de presupuestos y cuotas no tienen moneda definida en el modelo; se muestran sin inventar un símbolo. Las relaciones de pagos anteriores se conservan, pero este evento no las usa para descontar importes ni representar pagos parciales.

## Pruebas reproducibles

`mvn clean verify` ejecuta las pruebas unitarias. Para activar todas las pruebas de integración sobre la demo con los scripts 01/02/04/05:

```powershell
$env:ANALITIQ_CONFIG = (Resolve-Path '.local/analitiq.properties').Path
$env:ANALITIQ_TEST_DB = '1'
$env:ANALITIQ_TEST_DEUDAS = '1'
mvn clean verify
```

Se usa MySQL real y el usuario de sólo lectura. Las pruebas JDBC usan un Clock fijo en Java para validar ambos límites, vencimientos de hoy, cuotas futuras y la zona horaria Argentina. No hay un parámetro HTTP que permita alterar el día de corte. También se conserva la prueba de regresión del servicio anterior de pagos y la comprobación del rechazo de UPDATE.

Con Tomcat iniciado y Python 3 disponible:

```powershell
python scripts/probar-http.py http://127.0.0.1:8080/analitiq
```

La suite HTTP prueba JSP/Servlet/MySQL reales: búsqueda cero/uno/varios, validación de selección, rango conservado, filtros inclusivos, exclusiones, agrupación, tratamientos vacíos, inconsistencias, sesiones y pestañas separadas, CSRF, escape HTML y concurrencia. Está preparada para las fechas ficticias de 2026 y debe ejecutarse a partir del 15/09/2026. La comprobación de cuotas futuras se adapta al día real del servidor de pruebas (Argentina).

Los resultados Maven están en `target/surefire-reports`. La comprobación final y sus límites se documentan en [verificación](docs/verificacion.md).

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
