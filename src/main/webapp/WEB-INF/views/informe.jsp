<%@ include file="header.jspf" %>
<c:url var="cambiarPacienteUrl" value="/deudas"><c:param name="busqueda" value="${busquedaId}"/><c:param name="vista" value="seleccionar"/></c:url>
<div class="title-row"><div><h1>Ver informe de deudas de un paciente</h1><p class="lead">Cuotas pendientes de tratamientos activos para los criterios seleccionados.</p></div><a class="button secondary" href="<c:out value='${cambiarPacienteUrl}'/>">Cambiar paciente</a></div>
<section class="patient-card report-header"><div class="avatar" aria-hidden="true">P</div><div><h2><c:out value="${informe.paciente.nombre}"/> <c:out value="${informe.paciente.apellido}"/></h2><p>DNI <strong><c:out value="${informe.paciente.dni}"/></strong></p></div>
<dl class="report-range"><div><dt>Fecha desde</dt><dd><fmt:formatDate value="${informe.rango.desdeFecha}" pattern="dd/MM/yyyy"/></dd></div><div><dt>Fecha hasta</dt><dd><fmt:formatDate value="${informe.rango.hastaFecha}" pattern="dd/MM/yyyy"/></dd></div></dl>
</section>
<p class="applied-range">Tratamiento consultado: <strong><c:choose><c:when test="${empty informe.filtros.tratamiento}">Todos los activos</c:when><c:otherwise><c:out value="${informe.filtros.tratamiento}"/></c:otherwise></c:choose></strong></p>
<c:if test="${not empty mensaje}"><div class="notice" role="alert"><c:out value="${mensaje}"/></div></c:if>
<details class="panel filter-panel" ${not empty mensaje ? 'open' : ''}><summary>Modificar filtros</summary>
<form method="post" action="<c:url value='/deudas'/>"><input type="hidden" name="accion" value="filtrar"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>"><input type="hidden" name="busqueda" value="<c:out value='${busquedaId}'/>">
<%@ include file="tratamiento.jspf" %>
<%@ include file="rango.jspf" %>
<div class="form-footer"><span>Se conserva el paciente seleccionado.</span><button type="submit">Aplicar filtro</button></div></form></details>
<c:forEach var="aviso" items="${informe.avisos}"><div class="notice" role="alert"><c:out value="${aviso}"/></div></c:forEach>
<section class="report-section" aria-label="Deudas por tratamiento">
<c:if test="${empty informe.tratamientos}"><div class="empty" role="status">No se encontraron cuotas pendientes para los criterios seleccionados</div></c:if>
<c:forEach var="bloque" items="${informe.tratamientos}"><article class="panel treatment" data-tratamiento="<c:out value='${bloque.tratamiento.codigo}'/>">
<div class="section-heading"><h2><c:out value="${bloque.tratamiento.nombre}"/></h2><span class="badge">Tratamiento #<c:out value="${bloque.tratamiento.codigo}"/> · Activo</span></div>
<div class="table-wrap"><table><caption class="sr-only">Cuotas pendientes de <c:out value="${bloque.tratamiento.nombre}"/></caption><thead><tr><th>Cuota</th><th>Fecha de vencimiento</th><th class="amount">Saldo pendiente</th></tr></thead><tbody>
<c:forEach var="cuota" items="${bloque.cuotas}"><tr data-cuota="<c:out value='${cuota.codigo}'/>"><td><c:out value="${cuota.numero}"/></td><td><fmt:formatDate value="${cuota.vencimiento}" pattern="dd/MM/yyyy"/></td><td class="numeric amount"><fmt:formatNumber value="${cuota.monto}" minFractionDigits="2" maxFractionDigits="2"/></td></tr></c:forEach>
</tbody></table></div>
<dl class="debt-summary"><div><dt>Cantidad de cuotas pendientes</dt><dd data-cantidad="<c:out value='${bloque.cantidad}'/>"><c:out value="${bloque.cantidad}"/></dd></div><div><dt>Total de deuda del tratamiento en el rango</dt><dd class="numeric" data-total="<c:out value='${bloque.total}'/>"><fmt:formatNumber value="${bloque.total}" minFractionDigits="2" maxFractionDigits="2"/></dd></div></dl>
</article></c:forEach></section>
<p class="footnote">Saldo pendiente: importe completo de cada cuota. Moneda no especificada en el modelo: los totales suman los importes registrados por tratamiento, sin conversiones.</p>
<a class="back-link" href="<c:url value='/deudas'/>">← Iniciar otra búsqueda</a>
<%@ include file="footer.jspf" %>
