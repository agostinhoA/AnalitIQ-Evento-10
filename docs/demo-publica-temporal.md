# Demo pública temporal

El 28/09/2026 se habilitó un enlace HTTPS de prueba con Cloudflare Quick Tunnel. La URL actual está en `.local/public-demo-url.txt` (archivo privado del entorno). Es un enlace temporal: requiere computadora encendida, conexión a internet, MySQL, Tomcat y cloudflared activos. No es un despliegue permanente ni se configuró inicio automático al encender Windows.

Se creó una base independiente `analitiq_public_demo` desde los scripts 01/02/04/05/06/08: 17 pacientes ficticios, 22 tratamientos y 35 cuotas. No se copiaron los datos ingresados manualmente en la demo local. Las altas de los visitantes se guardan en esta base separada. No hay autenticación; cualquier persona que tenga el enlace puede consultar la demo y registrar tratamientos ficticios.

La instancia pública de Tomcat está en `.local/tomcat-public`, escucha en `127.0.0.1:8082` y usa `.local/public-demo.properties`. Sus cuentas MySQL propias tienen SELECT y, para registro, únicamente INSERT de las columnas permitidas de tratamientos. Las credenciales no se incluyen en Git. El puerto MySQL no se publica.

cloudflared 2026.9.3 se descargó del repositorio oficial Cloudflare y se verificó con SHA-256 `f096265ec2fcbe9bb6e2d64268db167ced3fcbb83d894bdb9e2fcdb26f2ea7e2`. El ejecutable está en `.tools/cloudflared.exe`, fuera de Git. El túnel apunta a la instancia 8082; no requiere abrir puertos entrantes en el router.

Tomcat reconoce `X-Forwarded-Proto` sólo desde loopback. Se comprobaron las cookies HTTPS `Secure; HttpOnly; SameSite=Lax`, las tres pantallas con HTTP 200, un informe real de tres cuotas (30.000,00), un alta con opcionales vacíos y la prevención de duplicados por reenvío. El alta creada durante la verificación se retiró por su código y token exactos para dejar disponible el caso manual.

## Datos para compartir

- Consultar DNI `46000001`, Desde `01/09/2026`, Hasta `30/11/2026`, todos los tratamientos: tres cuotas, total 30.000,00.
- Registrar DNI `30999888`, tipo Ortodoncia, fecha de inicio válida; los opcionales pueden quedar vacíos. Después de una primera alta exitosa, otro formulario con el mismo paciente/tipo debe rechazarse.
- Los siete DNI adicionales y sus casos están en [datos de prueba](siete-pacientes-prueba.md).

## Detener el enlace actual

Desde la raíz del proyecto, PowerShell:

```powershell
./scripts/detener-enlace-publico.ps1
```

El script verifica la identidad y fecha de inicio del proceso antes de detenerlo. La base pública conserva las altas realizadas.

## Volver a compartir

Si Tomcat público está detenido, iniciarlo en una terminal (JDK 17, MySQL activo):

```powershell
./scripts/ejecutar-tomcat.ps1 -TomcatHome .tools/apache-tomcat-9.0.122 -Port 8082 -BaseDirectory .local/tomcat-public -ConfigFile .local/public-demo.properties
```

En otra terminal, iniciar un túnel nuevo:

```powershell
./.tools/cloudflared.exe tunnel --url http://127.0.0.1:8082 --no-autoupdate --protocol http2
```

Copiar la nueva dirección HTTPS que muestra y agregar `/analitiq/`. La dirección cambia al crear otro túnel. En esta modalidad manual, Ctrl+C detiene el túnel; el archivo de URL y el script de cierre del proceso inicial no se actualizan automáticamente.

Referencia: [Cloudflare Quick Tunnels](https://developers.cloudflare.com/cloudflare-one/networks/connectors/cloudflare-tunnel/do-more-with-tunnels/trycloudflare/). Está destinado a pruebas y desarrollo; para publicación permanente se necesita otro esquema de alojamiento.
