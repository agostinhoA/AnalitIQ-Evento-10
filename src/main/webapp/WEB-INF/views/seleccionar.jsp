<%@ include file="header.jspf" %>
<h1>Ver informe de deudas de un paciente</h1><p class="lead">Seleccioná al paciente. Verificá su nombre, apellido y DNI antes de continuar.</p>
<ol class="steps"><li>1 <span>Buscar paciente</span></li><li class="current">2 <span>Identificar</span></li><li>3 <span>Ver informe</span></li></ol>
<c:if test="${not empty mensaje}"><div class="notice" role="alert"><c:out value="${mensaje}"/></div></c:if>
<section class="panel"><h2>Pacientes con cuotas pendientes</h2>
<p class="muted"><c:choose><c:when test="${flujo.rango.sinLimites}">Sin límite de fechas.</c:when><c:otherwise>Vencimientos<c:if test="${not empty flujo.rango.desde}"> desde el <fmt:formatDate value="${flujo.rango.desdeFecha}" pattern="dd/MM/yyyy"/></c:if><c:if test="${not empty flujo.rango.hasta}"> hasta el <fmt:formatDate value="${flujo.rango.hastaFecha}" pattern="dd/MM/yyyy"/></c:if>.</c:otherwise></c:choose> Tratamiento: <c:choose><c:when test="${empty flujo.filtros.tratamiento}">todos</c:when><c:otherwise><c:out value="${flujo.filtros.tratamiento}"/></c:otherwise></c:choose>. Los filtros se conservarán al continuar.</p>
<p class="muted">Cada paciente aparece una sola vez, con los tratamientos que tienen cuotas pendientes según los filtros seleccionados.</p>
<div class="patient-list">
<c:forEach var="paciente" items="${flujo.pacientes}">
<form class="patient-option" method="post" action="<c:url value='/deudas'/>">
<input type="hidden" name="accion" value="seleccionar"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>"><input type="hidden" name="busqueda" value="<c:out value='${busquedaId}'/>"><input type="hidden" name="dni" value="<c:out value='${paciente.dni}'/>">
<div class="patient-info"><h3><c:out value="${paciente.nombre}"/> <c:out value="${paciente.apellido}"/></h3><p>DNI <span class="numeric"><c:out value="${paciente.dni}"/></span></p>
<p class="patient-treatments"><strong>Tratamientos con deuda:</strong> <c:forEach var="tratamiento" items="${tratamientosPorPaciente[paciente.dni]}" varStatus="estado"><c:if test="${not estado.first}">, </c:if><span class="patient-treatment"><c:out value="${tratamiento}"/></span></c:forEach></p></div>
<button type="submit" class="secondary" aria-label="Seleccionar a <c:out value='${paciente.nombre}'/> <c:out value='${paciente.apellido}'/>, DNI <c:out value='${paciente.dni}'/>">Seleccionar →</button>
</form></c:forEach>
</div><a class="back-link" href="<c:url value='/deudas'/>">← Nueva búsqueda</a></section>
<%@ include file="footer.jspf" %>
