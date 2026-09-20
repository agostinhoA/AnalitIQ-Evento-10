# Verificación de la entrega

Fecha local: 18 de septiembre de 2026. Windows 11, Java 17.0.12, Maven 3.9.3, MySQL 8.4.7, Tomcat 9.0.122.

Esta verificación corresponde al evento 10 corregido: deudas de tratamientos activos, con rango obligatorio por vencimiento y exclusión de cuotas futuras. Reemplaza los resultados de la versión anterior de consulta general de pagos.

## Ejecutado y aprobado

- Maven `clean verify`: **BUILD SUCCESS**, **18 pruebas Java**, 0 fallos, 0 errores, 0 omitidas. Se activaron las integraciones con `ANALITIQ_TEST_DB=1` y `ANALITIQ_TEST_DEUDAS=1`, sobre MySQL real y los scripts 01/02/04/05.
- WAR generado en `target/analitiq.war` y desplegado en Tomcat 9.0.122 con contexto `/analitiq`. El artefacto desplegado es idéntico al generado.
- `scripts/probar-http.py`: **17 pruebas HTTP aprobadas**, usando formularios, Servlets, JSP compiladas por Tomcat y MySQL real.
- Búsqueda por DNI y nombre/apellido con cero/una/varias coincidencias; acceso directo cuando hay una; selección de homónimos validada en el servidor. Se conserva el rango incluso ante campos adicionales manipulados.
- Fechas obligatorias, calendario válido, orden del rango, rango de un solo día y ambos extremos incluidos. Rechazo HTTP 400 en solicitudes inválidas.
- Pruebas JDBC con reloj controlado: vencimiento del día incluido, día siguiente excluido, fechas futuras excluidas aunque entren en el rango, y cambio de día según Argentina.
- Exclusión de consultas odontológicas, cuotas Pagada y tratamientos inactivos. Los parámetros antiguos de tipo/estado no cambian el alcance.
- Sofía: tres tratamientos activos de distinto tipo, cuotas agrupadas por tratamiento y presupuesto, sin duplicados. Los bloques sin deuda permanecen visibles.
- Diferencia entre paciente sin tratamientos activos y tratamiento activo sin deudas en el rango. Presupuestos y cuotas muestran sus importes completos, sin calcular saldos parciales.
- Avisos de tipos repetidos y de más de tres activos: permanecen visibles los dos/cuatro registros de los casos deliberadamente inconsistentes.
- Aplicar un rango nuevo conserva al paciente validado y crea otro flujo; una pestaña anterior mantiene su rango. Un filtro inválido no reemplaza el rango del informe y muestra una explicación.
- Rechazo de DNI ajeno, flujo inexistente/de otra sesión, CSRF inválido y entradas malformadas; escape HTML. Sesiones independientes y 12 búsquedas repartidas entre 8 trabajadores concurrentes.
- Ruta principal `/deudas`, compatibilidad de `/pagos` con el alcance nuevo, página inicial y bloqueo de acceso directo a JSP privadas.
- Se conserva la prueba de regresión del servicio anterior de pagos. El usuario de aplicación sigue siendo de sólo lectura; el intento de UPDATE sin filas afectadas fue rechazado por MySQL con error 1142.
- Inspección del WAR: no contiene la contraseña local, configuración privada, carpetas .local/.tools ni scripts de base. `pom.properties` contiene sólo metadatos normales de Maven.
- Revisión visual en navegador local de la búsqueda y del informe de Sofía: rango aplicado y fecha de corte, campos de los tratamientos, tabla de cuotas y bloques sin deudas. Se conservan estilos y codificación de nombres y fechas.

SHA-256 del WAR entregado:

```text
6BA7E93A7672ED0DC1373C613065B454FBC346F61164D404DCC1665A6DB660E9
```

Reportes Maven: `target/surefire-reports`. Registros locales de ejecución: `tmp/coloquio-build.log` y `tmp/coloquio-http.log` (ignorados por Git). Antes de agregar los casos ficticios se guardó `.local/antes-coloquio.sql`; las adiciones no sobrescribieron filas previas. El WAR previo se conserva en `.local/analitiq-pre-deudas.war`.

## Alcance pendiente fuera de este entorno

- Abrir y ejecutar desde la interfaz de NetBeans: se verificaron su instalación y su Maven, pero se compiló por terminal y se desplegó directamente en Tomcat. README explica cómo registrar el servidor.
- Desplegar en hosting real, configurar dominio/HTTPS/JDBC con TLS y verificar migración y restore en otro servidor. No se publicó en internet.
- Carga a escala y revisión en navegadores/dispositivos adicionales. La concurrencia local no equivale a una certificación de rendimiento.
- Indisponibilidad durante una consulta y recuperación de infraestructura: el manejo HTTP 503 está implementado, pero no se detuvo el MySQL compartido para simularlo.
- Contrastar con Lucid, página 7, que no estaba disponible.
- Actualizar documentos externos del sistema: descripción del evento, DFD, ELP y diccionario. El detalle de los ajustes está en [coloquio del 17/09/2026](coloquio-2026-09-17.md); no se modificaron el PDF original ni Lucid.

La aplicación contiene datos ficticios y no incorpora autenticación del odontólogo. Antes de usar datos reales se requiere definir e implementar ese acceso.
