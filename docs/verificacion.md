# Verificación del evento 10

Fecha: 23/09/2026. Windows, Java 17.0.12, Maven 3.9.3 de NetBeans 19, MySQL WAMP 8.4.7 y Tomcat 9.0.122. Resultado correspondiente a la [especificación vigente](evento10-2026-09-23.md), que reemplaza la regla anterior de corte por hoy y bloques vacíos.

## Ejecutado

- **Maven clean verify: BUILD SUCCESS. 21 pruebas Java, cero fallos/errores/omitidas.** Se activaron ANALITIQ_TEST_DB y ANALITIQ_TEST_DEUDAS con la configuración externa existente. Se utilizó MySQL real; no H2 ni repositorios simulados.
- **20 pruebas HTTP aprobadas** contra el WAR desplegado en Tomcat. Se compilaron y ejecutaron las JSP reales.
- Identificación por DNI y por nombre/apellido, coincidencia única, homónimos, listado completo sin criterio, rechazo de criterios contradictorios/incompletos y búsqueda sin resultados.
- La lista de diez pacientes exige selección antes de mostrar cuotas. La variante de lista con un único paciente se comprobó en prueba unitaria de BusquedaSesion, sin alterar la base para reducirla.
- Validación de DNI seleccionado contra resultados de la sesión, conservación de tratamiento/fechas aunque se envíen campos manipulados, sesiones independientes, nueva búsqueda sin selección heredada y dos pestañas con filtros separados.
- Tratamiento omitido/específico/inexistente, exclusión de tratamientos inactivos, cuotas Pagada y pagos de consultas; filtro exacto y parametrizado. Se comprobó una cadena de inyección como valor literal de tratamiento.
- Rango obligatorio, fechas válidas, rango invertido, ambos límites incluidos y un solo día. Cuotas posteriores a hoy incluidas: por ejemplo 2308 con vencimiento 01/12/2026.
- Sin bloques vacíos; mensaje de ausencia de cuotas con identificación y rango, distinto de ausencia de pacientes. Avisos únicamente por tipos activos repetidos, incluso cuando no hay cuotas coincidentes. La corrección posterior elimina cualquier máximo numérico de tratamientos.
- Cantidades y totales coinciden con las filas HTML; sin duplicados, orden de vencimiento y dos decimales. Suma BigDecimal comprobada también con valores fraccionarios (10,10 + 20,20 = 30,30).
- Sofía (45000001), rango 01/01/2026–31/12/2027: Ortodoncia tiene cinco filas y total 50.000,00; Implante tiene una y total 20.000,00. El Conducto sin cuotas no se muestra.
- CSRF, escape HTML, entradas malformadas, acceso directo a JSP privadas y rutas /deudas, /pagos y página inicial.
- Concurrencia local: 12 búsquedas distribuidas entre 8 trabajadores.
- Se conserva la prueba de regresión de los componentes antiguos de pagos y el rechazo MySQL de UPDATE sin filas afectadas (error 1142), usando el usuario SELECT.
- Revisión visual en navegador local del formulario, tarjetas de selección y encabezado/tablas/totales de un informe con dos tratamientos.
- **Checksums idénticos en las 13 tablas antes y después de las pruebas HTTP y revisión visual.** No se aplicó ningún SQL de escritura ni migración en esta modificación.
- WAR generado y desplegado idénticos; no contiene contraseña local ni configuración privada/.local/.tools/scripts de base.

## Artefactos

WAR: `target/analitiq.war`, desplegado en `.local/tomcat/webapps/analitiq.war`.
Respaldo del WAR previo: `.local/analitiq-antes-20260923.war`.

SHA-256:

```text
7A7ADEDE1EE54ECEC3983B63C61609A405553D2FAB5C044464FEF4D8361B40EB
```

Registros locales: `tmp/evento10-20260923-build.log`, `tmp/evento10-20260923-http.log`, `tmp/evento10-20260923-checksum-antes.txt`, `tmp/evento10-20260923-checksum-despues.txt`. Reportes JUnit: `target/surefire-reports`.

## Límites de la verificación

- Se construyó con el Maven instalado de NetBeans y se desplegó en Tomcat; no se ejecutó desde la interfaz gráfica de NetBeans. README mantiene los pasos de configuración y ejecución.
- No se publicó en internet ni se probó migración a otro servidor. No se cambiaron las versiones del entorno ni las dependencias.
- No se interrumpió el MySQL compartido para simular un fallo durante esta suite. Los errores de configuración/SQL mantienen su manejo HTTP 503; la ausencia de datos produce mensajes distintos y HTTP 200.
- La revisión visual fue de escritorio; no equivale a pruebas de todos los dispositivos, navegadores o carga a escala.
- Las imágenes mencionadas en el pedido no estaban adjuntas. Se siguió la estructura escrita conservando el estilo actual.
- La moneda de cuotas/presupuestos sigue ausente del modelo. Las sumas son de sus valores registrados, sin símbolo ni conversiones; no se puede certificar contabilidad multimoneda con ese esquema.
- El TP/PDF, DFD, ELP y DD externos no fueron modificados. Los cambios pendientes están enumerados en la documentación del evento.

## Corrección de cantidad de tratamientos

Se recompiló y desplegó el WAR tras retirar el máximo numérico. Volvieron a pasar las 21 pruebas Java y las 20 HTTP. La prueba unitaria acepta cinco activos de tipos distintos; las pruebas MySQL/HTTP de Nora conservan únicamente el aviso por tipo repetido. No se ejecutaron migraciones ni se cambiaron datos. Registros de esta ejecución: tmp/regla-tratamientos-build.log y tmp/regla-tratamientos-http.log.


## Rediseño visual posterior

El WAR actual incorpora la paleta blanca/gris/roja y la navegación superior. Compilación correcta, 20 pruebas HTTP aprobadas y revisión visual en escritorio y móvil. No cambió Java ni la base. Detalle en [rediseño visual](rediseno-visual.md). Logs: tmp/rediseno-build.log y tmp/rediseno-http.log.
