<%@ include file="header.jspf" %>
<div class="title-row"><div><h1>Deudas de tratamientos activos</h1><p class="lead">Cuotas completas impagas, vencidas o que vencen hoy.</p></div><a class="button secondary" href="<c:url value='/deudas'/>">Cambiar paciente</a></div>
<section class="patient-card"><div class="avatar" aria-hidden="true">P</div><div><h2><c:out value="${informe.paciente.nombre}"/> <c:out value="${informe.paciente.apellido}"/></h2><p>DNI <strong><c:out value="${informe.paciente.dni}"/></strong></p></div><span class="badge">Sólo tratamientos activos</span></section>
<c:if test="${not empty mensaje}"><div class="notice" role="alert"><c:out value="${mensaje}"/></div></c:if>
<section class="panel filter-panel"><h2>Filtrar por vencimiento</h2>
<form method="post" action="<c:url value='/deudas'/>"><input type="hidden" name="accion" value="filtrar"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>"><input type="hidden" name="busqueda" value="<c:out value='${busquedaId}'/>">
<%@ include file="rango.jspf" %>
<div class="form-footer"><span>La consulta conserva al paciente seleccionado.</span><button type="submit">Aplicar filtro</button></div></form>
</section>
<p class="applied-range">Rango aplicado: <strong><fmt:formatDate value="${informe.rango.desdeFecha}" pattern="dd/MM/yyyy"/> — <fmt:formatDate value="${informe.rango.hastaFecha}" pattern="dd/MM/yyyy"/></strong>. Vencidas hasta hoy: <fmt:formatDate value="${informe.fechaCorte}" pattern="dd/MM/yyyy"/> (Argentina).</p>
<c:forEach var="aviso" items="${informe.avisos}"><div class="notice" role="alert"><c:out value="${aviso}"/></div></c:forEach>
<section class="report-section"><h2>Tratamientos activos</h2>
<c:if test="${empty informe.tratamientos}"><div class="empty">Este paciente no tiene tratamientos activos.</div></c:if>
<c:forEach var="bloque" items="${informe.tratamientos}"><article class="panel treatment" data-tratamiento="<c:out value='${bloque.tratamiento.codigo}'/>">
<div class="section-heading"><h3><c:out value="${bloque.tratamiento.nombre}"/></h3><span class="badge">Tratamiento #<c:out value="${bloque.tratamiento.codigo}"/> · Activo</span></div>
<p class="muted"><c:out value="${bloque.tratamiento.descripcion}" default="Sin descripción general."/></p>
<dl class="treatment-details"><div><dt>Fecha de inicio</dt><dd><fmt:formatDate value="${bloque.tratamiento.inicio}" pattern="dd/MM/yyyy"/></dd></div><div><dt>Finalización estimada</dt><dd><c:choose><c:when test="${empty bloque.tratamiento.finalizacionEstimada}">No registrada</c:when><c:otherwise><fmt:formatDate value="${bloque.tratamiento.finalizacionEstimada}" pattern="dd/MM/yyyy"/></c:otherwise></c:choose></dd></div><div><dt>Objetivos</dt><dd><c:out value="${bloque.tratamiento.objetivos}" default="No registrados"/></dd></div><div><dt>Pronóstico</dt><dd><c:out value="${bloque.tratamiento.pronostico}" default="No registrado"/></dd></div></dl>
<c:if test="${empty bloque.presupuestos}"><div class="empty">Este tratamiento activo no tiene deudas en el rango seleccionado.</div></c:if>
<c:forEach var="presupuesto" items="${bloque.presupuestos}"><section class="budget"><h4>Presupuesto #<c:out value="${presupuesto.codigo}"/></h4>
<dl class="budget-summary"><div><dt>Monto total del presupuesto</dt><dd><fmt:formatNumber value="${presupuesto.monto}" minFractionDigits="2" maxFractionDigits="2"/></dd></div><div><dt>Cuotas acordadas</dt><dd><c:out value="${presupuesto.cantidadCuotas}"/></dd></div><div><dt>Período de pago</dt><dd><c:out value="${presupuesto.periodo}"/></dd></div></dl>
<div class="table-wrap"><table><caption class="sr-only">Cuotas adeudadas del presupuesto <c:out value="${presupuesto.codigo}"/></caption><thead><tr><th>Cuota</th><th>Vencimiento</th><th>Importe completo adeudado</th><th>Estado registrado</th></tr></thead><tbody>
<c:forEach var="cuota" items="${presupuesto.cuotas}"><tr data-cuota="<c:out value='${cuota.codigo}'/>"><td><strong>Cuota <c:out value="${cuota.numero}"/></strong><br><small>#<c:out value="${cuota.codigo}"/></small></td><td><fmt:formatDate value="${cuota.vencimiento}" pattern="dd/MM/yyyy"/></td><td class="numeric"><fmt:formatNumber value="${cuota.monto}" minFractionDigits="2" maxFractionDigits="2"/></td><td><span class="status due"><c:out value="${cuota.estado}"/></span></td></tr></c:forEach>
</tbody></table></div></section></c:forEach>
</article></c:forEach></section>
<p class="footnote">Cada cuota se adeuda por su importe completo; no se calculan importes parciales. Los presupuestos y cuotas no tienen moneda especificada en el modelo.</p>
<a class="back-link" href="<c:url value='/deudas'/>">← Iniciar otra búsqueda</a>
<%@ include file="footer.jspf" %>

