<%@ include file="header.jspf" %>
<h1>No pudimos completar la consulta de deudas</h1><div class="panel"><p role="alert"><c:out value="${mensaje}" default="La página no está disponible o se produjo un error. Volvé a la búsqueda e intentá nuevamente."/></p><a class="button" href="<c:url value='/deudas'/>">Volver a buscar pacientes</a></div>
<%@ include file="footer.jspf" %>
