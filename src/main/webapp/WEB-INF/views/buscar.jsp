<%@ include file="header.jspf" %>
<h1>Ver informe de deudas de un paciente</h1><p class="lead">Consultá las cuotas pendientes de sus tratamientos activos dentro del rango elegido.</p>
<ol class="steps" aria-label="Pasos"><li class="current">1 <span>Buscar paciente</span></li><li>2 <span>Identificar</span></li><li>3 <span>Ver informe</span></li></ol>
<c:if test="${not empty mensaje}"><div class="notice" role="status"><c:out value="${mensaje}"/></div></c:if>
<div class="search-layout"><section class="panel"><h2>Datos de la consulta</h2>
<form method="post" action="<c:url value='/deudas'/>">
<input type="hidden" name="accion" value="buscar"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>">
<fieldset><legend>1. Identificar al paciente</legend>
<p class="muted" id="criterio-ayuda">Completá el DNI <strong>o</strong> el nombre y apellido juntos. Dejá los tres campos vacíos para elegir de la lista de pacientes.</p>
<div class="identification-fields">
<div><label for="dni">DNI</label><input id="dni" name="dni" type="text" inputmode="numeric" maxlength="8" placeholder="Ej. 30111222" value="<c:out value='${dniValor}'/>" aria-describedby="criterio-ayuda"><small>7 u 8 dígitos, sin puntos.</small></div>
<div class="two-columns"><div><label for="nombre">Nombre</label><input id="nombre" name="nombre" maxlength="100" placeholder="Ej. Juan" value="<c:out value='${nombreValor}'/>"></div><div><label for="apellido">Apellido</label><input id="apellido" name="apellido" maxlength="100" placeholder="Ej. Pérez" value="<c:out value='${apellidoValor}'/>"></div></div>
<small>La búsqueda por nombre y apellido es exacta, incluyendo mayúsculas y tildes.</small></div>
</fieldset><fieldset><legend>2. Filtrar las cuotas pendientes</legend>
<%@ include file="tratamiento.jspf" %>
<%@ include file="rango.jspf" %>
</fieldset>
<div class="form-footer"><span>Consulta de información, sin modificar registros.</span><button type="submit">Consultar →</button></div></form></section>
<aside class="help-card"><span class="help-icon" aria-hidden="true">i</span><h2>Un informe por paciente.</h2><p>Si la búsqueda identifica un único paciente, verás directamente su informe. Si hay homónimos o no ingresás un criterio, podrás elegirlo por su DNI.</p><hr><h3>Deudas por tratamiento</h3><p>Se incluyen cuotas en estado Adeuda con vencimiento dentro del rango, aunque todavía no hayan vencido. Cada cuota se adeuda por su importe completo.</p><p class="small">El tratamiento y las fechas se conservan durante la selección.</p></aside></div>
<%@ include file="footer.jspf" %>
