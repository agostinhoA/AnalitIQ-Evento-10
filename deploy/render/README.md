# AnalitIQ en Render Free + Aiven MySQL Free

Esta configuración publica la aplicación Java completa, con una base remota independiente
de WAMP. No exporta los registros locales ni incluye contraseñas en Git o en la imagen.
Usar exclusivamente datos ficticios: la aplicación todavía no tiene autenticación.

## 1. Preparar Aiven

Confirmar en Service settings que el plan sea **Free** y esperar estado Running.
Guardar el host y puerto de Connection information. Descargar el certificado CA del
proyecto desde la consola de Aiven. No es el certificado de cliente ni una clave privada.

Desde un cliente MySQL 8.4, conectarse con el administrador proporcionado por Aiven,
solicitando la contraseña de forma interactiva (nunca escribirla en el comando):

```text
mysql --host=HOST_AIVEN --port=PUERTO_AIVEN --user=avnadmin --password --ssl-mode=VERIFY_IDENTITY --ssl-ca=RUTA_CA --default-character-set=utf8mb4
```

Antes de importar, comprobar `SHOW DATABASES;`. El esquema `analitiq_demo` debe ser
nuevo: `01-estructura.sql` falla si ya existe. No borrar ni reemplazar una base existente.
Si ya existe, revisar qué migraciones y datos contiene antes de continuar.

Ejecutar una sola vez, en este orden, deteniéndose ante cualquier error:

1. `database/01-estructura.sql` (estructura y dependencias).
2. `database/02-datos-prueba.sql` (catálogos y pacientes iniciales).
3. `database/04-datos-prueba-deudas.sql` (casos del informe).
4. `database/05-datos-prueba-historicos.sql` (históricos válidos).
5. `database/06-evento3.sql` (registro de tratamientos y restricciones).
6. `database/08-siete-pacientes-prueba.sql` (siete pacientes adicionales).

No ejecutar con `--force`. Usar un cliente que detenga la ejecución ante error; si falla
una carga transaccional, hacer ROLLBACK y revisar antes de continuar. Estos scripts no
son idempotentes y no deben ejecutarse en cada arranque o despliegue.

Crear las dos cuentas de aplicación usando una copia privada de
`usuarios.sql.example`, con contraseñas distintas y robustas. No usar `avnadmin`
como cuenta de ejecución de AnalitIQ. Limitar en Aiven las IP permitidas a las salidas
de Render (panel del servicio, información de conexiones) y, durante la preparación,
a la IP administrativa necesaria. No modificar los usuarios ni la base de WAMP.

## 2. Crear el Web Service en Render

Conectar GitHub y seleccionar `agostinhoA/AnalitIQ-Evento-10` cuando estos archivos
ya estén publicados en el repositorio. Elegir:

| Campo | Valor |
| --- | --- |
| Name | `analitiq` o un nombre disponible |
| Branch | `master` |
| Language / Runtime | `Docker` |
| Root Directory | Vacío (raíz del repositorio) |
| Dockerfile Path | `./Dockerfile` |
| Docker Command | Vacío (usa el comando de la imagen) |
| Instance Type | **Free**, USD 0 |
| Health Check Path | `/inicio` |

El contenedor utiliza el puerto definido por `PORT` (10000 por defecto), escucha en
0.0.0.0 y publica `ROOT.war`. En Render la dirección es `https://NOMBRE.onrender.com/`,
sin `/analitiq`. El contexto de la ejecución local sigue siendo `/analitiq`.

En Environment, configurar cinco variables:

```text
ANALITIQ_DB_URL=jdbc:mysql://HOST_AIVEN:PUERTO_AIVEN/analitiq_demo?sslMode=VERIFY_IDENTITY&connectionTimeZone=America/Argentina/Buenos_Aires
ANALITIQ_DB_USER=analitiq_lectura
ANALITIQ_DB_PASSWORD=REEMPLAZAR_CLAVE_LECTURA
ANALITIQ_DB_WRITE_USER=analitiq_registro
ANALITIQ_DB_WRITE_PASSWORD=REEMPLAZAR_CLAVE_REGISTRO
```

Pegar valores sin comillas. Usar las contraseñas creadas para las cuentas restringidas,
no la contraseña administrativa. No configurar `ANALITIQ_CONFIG` con una ruta de Windows.

En Secret Files, agregar **aiven-ca.pem** con el contenido del certificado CA descargado.
Render lo monta en `/etc/secrets/aiven-ca.pem`; el inicio importa ese certificado a un
almacén temporal y Connector/J valida la identidad del servidor. La contraseña fija
`changeit` protege únicamente ese almacén de certificados públicos; no es una clave MySQL.
No desactivar TLS para solucionar errores de conexión.

Después de preparar base, usuarios, variables y certificado, pulsar Deploy Web Service.
El Dockerfile compila el WAR en Render y ejecuta Tomcat 9 con Java 17, como usuario sin
privilegios. `.dockerignore` limita lo enviado al build a código y configuración pública.
No hay importaciones ni modificaciones de datos automáticas al arrancar.

## 3. Comprobar el despliegue

- Esperar estado Live y abrir el enlace que muestre Render.
- Abrir el informe y probar DNI `45000001` (Sofía Molina), sin filtro de fechas:
  en el seed original hay dos tratamientos con deuda por un total de 70000.
- Probar Ana López (DNI `46000001` y `46000002`) para comprobar la selección.
- Probar DNI `30999888` para ausencia de deudas activas.
- Probar registro con un paciente que no tenga activo el tipo elegido, por ejemplo
  `46000001` e Implante en el seed original. Esta prueba sí crea un tratamiento.
- Comprobar subtotales, total general, navegación y cookie de sesión Secure/HttpOnly.
- Abrir una segunda sesión de navegador para verificar que no comparta la búsqueda.

Los datos de prueba tienen fechas fijas de 2026. Para verlos todos dejar las fechas
vacías; para un ejemplo acotado usar 01/09/2026 a 30/11/2026. Reinicios y despliegues
invalidan las búsquedas en sesión: empezar una nueva, sin reutilizar enlaces con `busqueda`.
El health check `/inicio` comprueba que Tomcat sirve la interfaz, no la conexión MySQL.
El estado Live por sí solo no prueba el funcionamiento del sistema: realizar una
búsqueda y registrar un tratamiento para validar ambas conexiones y sus permisos.

## Límites y mantenimiento

Render Free puede suspenderse después de 15 minutos sin tráfico; el primer acceso
posterior tarda en arrancar. Tiene límites mensuales. Aiven Free puede apagar servicios
inactivos según sus condiciones. No es una garantía de disponibilidad continua.
La computadora local puede estar apagada: aplicación y datos están en servicios remotos.
Si una pantalla exige tarjeta o un plan pago, no continuar dentro de este presupuesto.

Las etiquetas de las imágenes siguen las actualizaciones de Java 17 / Tomcat 9.
Revisar actualizaciones y compatibilidad antes de reconstruir. Para cambiar la interfaz,
publicar los cambios en la rama conectada y desplegar de nuevo; los datos permanecen
en Aiven. Hacer copias de seguridad de la base y verificar su recuperación por separado.

Referencias: [Docker en Render](https://render.com/docs/docker),
[Web Services](https://render.com/docs/web-services),
[Render Free](https://render.com/docs/free),
[Aiven MySQL Free](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier),
[certificados Aiven](https://aiven.io/docs/platform/concepts/tls-ssl-certificates).
