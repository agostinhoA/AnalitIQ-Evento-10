<%@ include file="header.jspf" %>
<c:url var="cambiarPacienteUrl" value="/deudas"><c:param name="busqueda" value="${busquedaId}"/><c:param name="vista" value="seleccionar"/></c:url>
<div class="title-row"><div><h1>Ver informe de deudas de un paciente</h1><p class="lead">Los importes adeudados suman las cuotas impagas con vencimiento hasta el día de la consulta, inclusive.</p></div><a class="button secondary" href="<c:out value='${cambiarPacienteUrl}'/>">Cambiar paciente</a></div>
<section class="patient-card report-header">
<p>DNI del paciente <strong><c:out value="${informe_deudas_tratamiento_paciente.paciente.dni}"/></strong></p>
<c:if test="${not informe_deudas_tratamiento_paciente.rango.sinLimites}"><dl class="report-range"><div><dt>Fecha desde</dt><dd><c:choose><c:when test="${empty informe_deudas_tratamiento_paciente.rango.desde}">Sin límite</c:when><c:otherwise><fmt:formatDate value="${informe_deudas_tratamiento_paciente.rango.desdeFecha}" pattern="dd/MM/yyyy"/></c:otherwise></c:choose></dd></div><div><dt>Fecha hasta</dt><dd><c:choose><c:when test="${empty informe_deudas_tratamiento_paciente.rango.hasta}">Sin límite</c:when><c:otherwise><fmt:formatDate value="${informe_deudas_tratamiento_paciente.rango.hastaFecha}" pattern="dd/MM/yyyy"/></c:otherwise></c:choose></dd></div></dl></c:if>
</section>
<c:if test="${not empty mensaje}"><div class="notice" role="alert"><c:out value="${mensaje}"/></div></c:if>
<c:forEach var="aviso" items="${informe_deudas_tratamiento_paciente.avisos}"><div class="notice" role="alert"><c:out value="${aviso}"/></div></c:forEach>
<section class="report-section" aria-label="Deudas por tratamiento">
<c:if test="${empty informe_deudas_tratamiento_paciente.tratamientos}"><div class="empty" role="status">No se encontraron cuotas vencidas para los criterios seleccionados.</div></c:if>
<c:forEach var="bloque" items="${informe_deudas_tratamiento_paciente.tratamientos}"><article class="panel treatment" data-tratamiento="<c:out value='${bloque.tratamiento.codigo}'/>">
<div class="section-heading"><h2><c:out value="${bloque.tratamiento.nombre}"/></h2></div>
<div class="table-wrap"><table>
<caption class="sr-only">Cuotas vencidas de <c:out value="${bloque.tratamiento.nombre}"/></caption>
<thead><tr><th scope="col">N.º de cuota</th><th scope="col">Fecha de vencimiento</th><th scope="col" class="amount">Saldo pendiente</th></tr></thead>
<tbody>
<c:forEach var="cuota" items="${bloque.cuotas}">
  <tr data-cuota="<c:out value='${cuota.codigo}'/>">
    <td><c:out value="${cuota.numero}"/></td>
    <td><fmt:formatDate value="${cuota.vencimiento}" pattern="dd/MM/yyyy"/></td>
    <td class="numeric amount"><fmt:formatNumber value="${cuota.monto}" minFractionDigits="2" maxFractionDigits="2"/></td>
  </tr>
</c:forEach>
</tbody>
</table></div>
<dl class="debt-summary">
  <div><dt>Cantidad cuotas vencidas</dt><dd data-cantidad="<c:out value='${bloque.cantidad}'/>"><c:out value="${bloque.cantidad}"/></dd></div>
  <div><dt>Subtotal adeudado</dt><dd class="numeric" data-total="<c:out value='${bloque.total}'/>"><fmt:formatNumber value="${bloque.total}" minFractionDigits="2" maxFractionDigits="2"/></dd></div>
</dl>
</article></c:forEach></section>
<section class="panel report-total" aria-labelledby="total-general-titulo">
<h2 id="total-general-titulo">Total adeudado</h2>
<p class="numeric report-total-amount" data-total-general="<c:out value='${informe_deudas_tratamiento_paciente.totalGeneral}'/>"><fmt:formatNumber value="${informe_deudas_tratamiento_paciente.totalGeneral}" minFractionDigits="2" maxFractionDigits="2"/></p>
</section>
<a class="back-link" href="<c:url value='/deudas'/>">← Iniciar otra búsqueda</a>
<%@ include file="footer.jspf" %>
