<%@ include file="header.jspf" %>
<h1>Tu consultorio, en un solo lugar.</h1>
<p class="lead">Elegí qué querés hacer para continuar con la atención de tus pacientes.</p>
<div class="menu-grid">
<section class="panel menu-card"><span class="menu-number">01 / TRATAMIENTOS</span><h2>Registrar un tratamiento</h2><p>Iniciá un tratamiento para un paciente registrado. Indicá el tipo, las fechas y los objetivos de la atención.</p><a class="button" href="<c:url value='/tratamientos/registrar'/>">Registrar tratamiento →</a></section>
<section class="panel menu-card"><span class="menu-number">02 / INFORMES</span><h2>Ver informe de deudas</h2><p>Consultá las cuotas pendientes de los tratamientos de un paciente, con fechas de vencimiento opcionales.</p><a class="button secondary" href="<c:url value='/deudas'/>">Consultar informe →</a></section>
</div>
<p class="footnote">Entorno de demostración. Usá únicamente datos ficticios.</p>
<%@ include file="footer.jspf" %>
