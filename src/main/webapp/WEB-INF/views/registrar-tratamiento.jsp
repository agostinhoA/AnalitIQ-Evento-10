<%@ include file="header.jspf" %>
<h1>Registrar un tratamiento</h1><p class="lead">Completá los datos para iniciar el tratamiento de un paciente.</p>
<c:if test="${not empty mensaje}"><div class="notice" role="alert"><c:out value="${mensaje}"/></div></c:if>
<div class="search-layout"><section class="panel"><h2>Datos del tratamiento</h2>
<p class="muted">Los campos marcados con * son obligatorios.</p>
<form method="post" action="<c:url value='/tratamientos/registrar'/>">
<input type="hidden" name="csrf" value="<c:out value='${sessionScope.registroSesion.csrf}'/>">
<input type="hidden" name="token" value="<c:out value='${tokenValor}'/>">
<fieldset><legend>1. Paciente y tratamiento</legend><div class="registration-fields">
<div><label for="dni">DNI del paciente *</label><input class="field-short" id="dni" name="dni" required pattern="[0-9]{7,8}" inputmode="numeric" maxlength="8" placeholder="Ej. 30999888" value="<c:out value='${dniValor}'/>" aria-describedby="dni-ayuda"><small id="dni-ayuda">7 u 8 dígitos, sin puntos. El paciente debe estar registrado.</small></div>
<div><label for="tipo">Nombre del tratamiento *</label><select class="field-treatment" id="tipo" name="tipo" required><option value="">Seleccioná un tratamiento</option><c:if test="${tipoNoDisponible}"><option value="<c:out value='${tipoValor}'/>" selected>Selección enviada: <c:out value="${tipoValor}"/> (revisar)</option></c:if><c:forEach var="tipo" items="${tipos}"><option value="${tipo.codigo}" ${not tipoNoDisponible and tipoValor == tipo.codigo.toString() ? 'selected' : ''}><c:out value="${tipo.nombre}"/></option></c:forEach></select></div>
</div></fieldset>
<fieldset><legend>2. Fechas</legend><div class="date-fields">
<div><label for="inicio">Fecha de inicio *</label><input class="field-date" id="inicio" name="inicio" type="date" required min="1000-01-01" max="9999-12-31" value="<c:out value='${inicioValor}'/>"></div>
<div><label for="fin">Finalización estimada <small>(opcional)</small></label><input class="field-date" id="fin" name="fin" type="date" min="1000-01-01" max="9999-12-31" value="<c:out value='${finValor}'/>"></div>
</div><p class="range-help">Si indicás una finalización estimada, debe ser igual o posterior al inicio.</p></fieldset>
<fieldset><legend>3. Plan de atención <small>(opcional)</small></legend><div class="registration-fields">
<div><label for="objetivos">Objetivos <small>(opcional)</small></label><textarea class="textarea-wide" id="objetivos" name="objetivos" rows="4" maxlength="65535" aria-describedby="texto-ayuda" placeholder="¿Qué se busca lograr con el tratamiento?"><c:out value="${objetivosValor}"/></textarea></div>
<div><label for="pronostico">Pronóstico <small>(opcional)</small></label><textarea class="textarea-wide" id="pronostico" name="pronostico" rows="4" maxlength="65535" aria-describedby="texto-ayuda" placeholder="Describí la evolución esperada"><c:out value="${pronosticoValor}"/></textarea></div>
</div><small id="texto-ayuda">Podés dejar ambos campos vacíos y completar sólo los datos obligatorios.</small></fieldset>
<div class="form-footer"><a href="<c:url value='/inicio'/>">Volver al menú</a><button type="submit">Registrar tratamiento</button></div>
</form></section>
<aside class="help-card"><span class="help-icon" aria-hidden="true">+</span><h2>Un nuevo paso en la atención.</h2><p>El tratamiento se registrará como activo. El paciente puede tener distintos tipos de tratamiento, sin límite de cantidad.</p><hr><h3>Un activo por tipo</h3><p>Si ya tiene uno activo del tipo elegido, primero debe finalizarlo mediante el proceso correspondiente. Los tratamientos anteriores inactivos no impiden una nueva alta.</p><p>El presupuesto, las cuotas y los pagos se gestionan en sus respectivos eventos.</p></aside></div>
<%@ include file="footer.jspf" %>
