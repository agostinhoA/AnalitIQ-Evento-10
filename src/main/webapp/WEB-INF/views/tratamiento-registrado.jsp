<%@ include file="header.jspf" %>
<h1>Tratamiento registrado</h1><p class="lead" role="status">El tratamiento se guardó correctamente y quedó activo.</p>
<section class="panel confirmation" data-registro="${registro.codigo}"><h2><c:out value="${registro.tipo.nombre}"/></h2>
<dl class="treatment-details"><div><dt>Paciente</dt><dd><c:out value="${registro.paciente.nombre}"/> <c:out value="${registro.paciente.apellido}"/></dd></div><div><dt>DNI</dt><dd><c:out value="${registro.paciente.dni}"/></dd></div><div><dt>Código del tratamiento</dt><dd><c:out value="${registro.codigo}"/></dd></div><div><dt>Estado</dt><dd>Activo</dd></div></dl>
<p class="muted">Todavía no tiene presupuesto ni cuotas. No aparecerá como deuda hasta que tenga cuotas pendientes.</p>
<div class="confirmation-actions"><a class="button" href="<c:url value='/tratamientos/registrar'/>">Registrar otro tratamiento</a><a class="button secondary" href="<c:url value='/inicio'/>">Volver al menú</a></div></section>
<%@ include file="footer.jspf" %>
