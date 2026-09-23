# Rediseño visual: blanco, gris oscuro y rojo

Referencia elegida por el usuario: [página principal de Start Bootstrap](https://startbootstrap.com/), no una plantilla administrativa específica.

Se adaptaron la paleta clara, los acentos rojos y la navegación superior. No se descargó una plantilla ni se agregaron Bootstrap, fuentes remotas, JavaScript o dependencias.

## Archivos

- `src/main/webapp/assets/theme.css`: tema visual sobre los estilos base; colores, tipografía de sistema, tarjetas, tablas, totales, estados de foco y adaptación a pantallas pequeñas.
- `src/main/webapp/WEB-INF/views/header.jspf`: navegación superior, marca y carga del tema local.
- `target/analitiq.war`: WAR regenerado y desplegado.

La base y las reglas Java no cambian. Los formularios, filtros, validaciones y selección de pacientes conservan su comportamiento.

## Verificación

- Maven package: BUILD SUCCESS. Se omitieron las pruebas unitarias porque no cambió código Java.
- Los 20 recorridos HTTP existentes pasaron contra Tomcat y MySQL con el encabezado nuevo.
- Revisión visual de formulario, lista y reporte en escritorio; formulario y reporte también en viewport móvil de 390 × 844. El contenido no desborda horizontalmente.
- Ajuste final de separación entre formulario y ayuda en móvil; se regeneró el WAR.
- No hace falta ejecutar SQL. Respaldo previo en `.local/analitiq-antes-rediseno.war`.

Paleta principal: rojo #c9322a, texto #34383e, fondo #f3f5fa y tarjetas blancas. CSS servido localmente, sin necesitar conexión a Start Bootstrap para usar AnalitIQ.
