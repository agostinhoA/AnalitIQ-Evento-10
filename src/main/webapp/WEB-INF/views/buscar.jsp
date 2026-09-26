<%@ include file="header.jspf" %>
<h1>Ver informe de deudas de un paciente</h1><p class="lead">Consultá las cuotas pendientes de tratamientos activos, con fechas opcionales.</p>
<ol class="steps" aria-label="Pasos"><li class="current">1 <span>Buscar paciente</span></li><li>2 <span>Identificar</span></li><li>3 <span>Ver informe</span></li></ol>

<div class="search-layout"><section class="panel"><h2>Datos de la consulta</h2>
<form id="debt-search" method="post" action="<c:url value='/deudas'/>" data-additional="${additionalFiltersValor}">
<input type="hidden" name="accion" value="buscar"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrf}'/>">
<fieldset><legend>1. Elegí cómo querés buscar</legend>
<p class="muted">Seleccioná un criterio principal. Después podés sumar filtros opcionales.</p>
<div class="search-modes">
<label><input type="radio" name="searchMode" value="all" ${empty searchModeValor or searchModeValor == 'all' ? 'checked' : ''}><span>Todos</span></label>
<label><input type="radio" name="searchMode" value="dni" ${searchModeValor == 'dni' ? 'checked' : ''}><span>DNI</span></label>
<label><input type="radio" name="searchMode" value="name" ${searchModeValor == 'name' ? 'checked' : ''}><span>Nombre y/o apellido</span></label>
<label><input type="radio" name="searchMode" value="advanced" ${searchModeValor == 'advanced' ? 'checked' : ''}><span>Búsqueda avanzada</span></label>
</div></fieldset>
<p id="all-message">Se mostrarán todos los pacientes con cuotas pendientes.</p>
<fieldset id="patient-fields" hidden><legend>2. Datos de búsqueda</legend>
<div id="dni-fields" hidden><label for="dni">DNI del paciente</label><input class="field-short" id="dni" name="dni" type="text" inputmode="numeric" maxlength="8" pattern="[0-9]{7,8}" placeholder="Ej. 30111222" value="<c:out value='${dniValor}'/>" aria-describedby="dni-help search-error"><small id="dni-help">7 u 8 dígitos, sin puntos.</small></div>
<div id="name-fields" class="identification-fields" hidden><div class="two-columns">
<div><label for="nombre">Nombre</label><input class="field-medium" id="nombre" name="nombre" maxlength="100" placeholder="Ej. Juan" value="<c:out value='${nombreValor}'/>" aria-describedby="name-help search-error"></div>
<div><label for="apellido">Apellido</label><input class="field-medium" id="apellido" name="apellido" maxlength="100" placeholder="Ej. Pérez" value="<c:out value='${apellidoValor}'/>" aria-describedby="name-help search-error"></div></div>
<p class="range-help" id="name-help">Podés buscar por nombre, apellido o ambos. Si existen varias coincidencias, podrás seleccionar el paciente de la lista de resultados.</p></div>
</fieldset>
<button id="toggle-filters" type="button" class="secondary" aria-expanded="false" aria-controls="optional-filters" hidden>+ Agregar filtros</button>
<input type="hidden" id="additional-filters" name="additionalFilters" value="false">
<fieldset id="optional-filters" hidden><legend id="filters-heading">Filtros adicionales</legend>
<%@ include file="tratamiento.jspf" %>
<%@ include file="rango.jspf" %>
</fieldset>
<p id="search-error" class="notice" role="alert" ${empty mensaje ? 'hidden' : ''}><c:out value="${mensaje}"/></p>
<p id="advanced-message" class="range-help" hidden>Si no aplicás ningún filtro, se mostrarán todos los pacientes con cuotas pendientes.</p>
<div class="form-footer"><span>Consulta de información, sin modificar registros.</span><button type="submit">Consultar →</button></div></form></section>
<aside class="help-card"><span class="help-icon" aria-hidden="true">i</span><h2>Un informe por paciente.</h2><p>La búsqueda muestra sólo pacientes con cuotas pendientes y los agrupa por tratamiento. Si dejás las fechas vacías, se consultan todos los vencimientos.</p><hr><h3>Deudas por tratamiento</h3><p>Se incluyen cuotas en estado Adeuda de los tratamientos activos, aunque todavía no hayan vencido. Cada cuota se adeuda por su importe completo.</p><p class="small">El tratamiento y las fechas se conservan durante la selección.</p></aside></div>
<script charset="UTF-8" src="<c:url value='/assets/debt-search.js'/>" defer></script>
<%@ include file="footer.jspf" %>
