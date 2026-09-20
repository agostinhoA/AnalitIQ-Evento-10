<%@ include file="header.jspf" %>
<h1>Consulta de deudas</h1><p class="lead">Encontrá a tu paciente y consultá las cuotas adeudadas de sus tratamientos activos.</p>
<ol class="steps" aria-label="Pasos"><li class="current">1 <span>Buscar paciente</span></li><li>2 <span>Identificar</span></li><li>3 <span>Consultar información</span></li></ol>
<c:if test="${not empty mensaje}"><div class="notice" role="status"><c:out value="${mensaje}"/></div></c:if>
<div class="search-layout"><section class="panel"><h2>Buscar paciente</h2><p class="muted">Elegí un criterio de búsqueda e ingresá los datos completos.</p>
<form method="post" action="<c:url value='/deudas'/>">
<input type="hidden" name="accion" value="buscar"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>">
<fieldset><legend>1. ¿Cómo querés buscar?</legend>
<div class="search-option"><label class="radio-label"><input type="radio" name="modo" value="dni" ${param.modo ne 'nombre' ? 'checked' : ''}> Por DNI</label>
<div class="option-fields"><label for="dni">DNI del paciente</label><input id="dni" name="dni" type="text" inputmode="numeric" maxlength="8" placeholder="Ej. 30111222" value="<c:out value='${param.dni}'/>"><small>7 u 8 dígitos, sin puntos.</small></div></div>
<div class="search-option"><label class="radio-label"><input type="radio" name="modo" value="nombre" ${param.modo eq 'nombre' ? 'checked' : ''}> Por nombre y apellido</label>
<div class="option-fields two-columns"><div><label for="nombre">Nombre completo</label><input id="nombre" name="nombre" maxlength="100" placeholder="Ej. Juan" value="<c:out value='${param.nombre}'/>"></div><div><label for="apellido">Apellido completo</label><input id="apellido" name="apellido" maxlength="100" placeholder="Ej. Pérez" value="<c:out value='${param.apellido}'/>"></div><small>Coincidencia exacta, incluyendo mayúsculas y tildes.</small></div></div>
</fieldset><fieldset><legend>2. Rango de vencimientos</legend>
<%@ include file="rango.jspf" %>
</fieldset>
<div class="form-footer"><span>Consulta de información, sin modificar registros.</span><button type="submit">Buscar paciente <span aria-hidden="true">→</span></button></div></form></section>
<aside class="help-card"><span class="help-icon" aria-hidden="true">i</span><h2>Deudas organizadas por tratamiento.</h2><p>Se muestran cuotas impagas que vencieron o vencen hoy, dentro del rango elegido. Cada cuota se presenta por su importe completo.</p><hr><h3>¿Hay pacientes con el mismo nombre?</h3><p>Te mostraremos sus DNI para que puedas identificar al paciente correcto.</p><p class="small">El rango se conserva al seleccionar al paciente. Si hay una única coincidencia, accederás directamente a sus deudas.</p></aside></div>
<%@ include file="footer.jspf" %>
