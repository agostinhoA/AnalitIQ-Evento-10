<%@ include file="header.jspf" %>
<h1>Seleccioná al paciente</h1><p class="lead">Encontramos varias coincidencias. Verificá el DNI antes de continuar.</p>
<ol class="steps"><li>1 <span>Buscar paciente</span></li><li class="current">2 <span>Identificar</span></li><li>3 <span>Consultar información</span></li></ol>
<c:if test="${not empty mensaje}"><div class="notice" role="alert"><c:out value="${mensaje}"/></div></c:if>
<section class="panel"><div class="section-heading"><h2>Pacientes encontrados</h2><span class="badge">Deudas de tratamientos activos</span></div>
<p class="muted">Vencimientos del <fmt:formatDate value="${flujo.rango.desdeFecha}" pattern="dd/MM/yyyy"/> al <fmt:formatDate value="${flujo.rango.hastaFecha}" pattern="dd/MM/yyyy"/>. El rango se conservará al continuar.</p>
<form method="post" action="<c:url value='/deudas'/>">
<input type="hidden" name="accion" value="seleccionar"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>"><input type="hidden" name="busqueda" value="<c:out value='${busquedaId}'/>">
<fieldset><legend class="sr-only">Elegí un paciente</legend><div class="table-wrap"><table><thead><tr><th>Seleccionar</th><th>Nombre</th><th>Apellido</th><th>DNI</th></tr></thead><tbody>
<c:forEach var="paciente" items="${flujo.pacientes}"><tr><td><input type="radio" name="dni" value="<c:out value='${paciente.dni}'/>" id="p<c:out value='${paciente.dni}'/>" required aria-label="Seleccionar DNI <c:out value='${paciente.dni}'/>"></td><td><label for="p<c:out value='${paciente.dni}'/>"><c:out value="${paciente.nombre}"/></label></td><td><c:out value="${paciente.apellido}"/></td><td class="numeric"><c:out value="${paciente.dni}"/></td></tr></c:forEach>
</tbody></table></div></fieldset><div class="form-footer"><a href="<c:url value='/deudas'/>">Nueva búsqueda</a><button type="submit">Consultar deudas →</button></div></form></section>
<%@ include file="footer.jspf" %>
