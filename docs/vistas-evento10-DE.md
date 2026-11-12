# Correspondencia de vistas del evento 10

Las tres JSP se mantienen físicamente separadas en `src/main/webapp/WEB-INF/views/`.

| Módulo del DE | Archivo JSP |
| --- | --- |
| 13: Solicitud_de_Informe_Deudas | VistaJSP_Recibir_solicitud_de_Informe_Deuda.jsp |
| 2: Obtener_Datos_Para_La_Consulta_De_Deudas | VistaJSP_Obtener_Datos_Para_Consulta_Deudas.jsp |
| 10: Presentar_Resultados_De_La_Consulta_De_Deudas | VistaJSP_Ver_Informe_De_Deudas.jsp |

La clase servlet 13 es `ar.com.analitiq.web.Controlador_Consulta_De_Deudas`.
El nombre registrado del servlet es `Consulta_Deudas_Servlet` (módulo 19).
Sus entradas HTTP son `doGet` y `doPost`, según el contrato de Servlet.
Todas las vistas envían sus formularios o enlaces al mismo controlador mediante
`/deudas`; `/pagos` conserva compatibilidad con enlaces anteriores.

`informacion_para_deudas` es el flujo lógico del formulario: el transporte HTTP
conserva `searchMode`, `dni`, `nombre`, `apellido`, `additionalFilters`,
`tratamiento`, `desde` y `hasta`, además de `accion` y el token técnico `csrf`.
No es un campo HTTP único ni un objeto serializado con ese nombre.

La selección recibe el atributo `lista_pacientes`, y los tratamientos por paciente
en `tratamientosPorPaciente`. `datos_consulta_deudas` corresponde a la selección
enviada mediante `dni` y `busqueda`; el servidor recupera los filtros de la sesión
y valida que el paciente pertenezca a los resultados de esa búsqueda.

La vista de informe recibe `informe_deudas_tratamiento_paciente` como atributo
de solicitud. Las rutas de forward incluyen `/WEB-INF/views/` y el nombre físico
completo de cada JSP. No se utiliza un atributo `etapa` ni una JSP única para
alternar entre pantallas. Los identificadores internos buscar/seleccionar/informe
del controlador solo resuelven forwards a esos archivos distintos.
