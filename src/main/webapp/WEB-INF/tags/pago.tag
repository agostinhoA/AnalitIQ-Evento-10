<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="pago" required="true" type="ar.com.analitiq.model.Pago" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="payment"><div><strong>Pago #<c:out value="${pago.codigo}"/></strong><span><fmt:formatDate value="${pago.fecha}" pattern="dd/MM/yyyy"/></span></div>
<dl><div><dt>Importe del pago</dt><dd><fmt:formatNumber value="${pago.monto}" minFractionDigits="2" maxFractionDigits="2"/> <c:out value="${pago.moneda}"/></dd></div><div><dt>Método</dt><dd><c:out value="${pago.metodo}"/></dd></div><div><dt>Cotización aplicada</dt><dd><fmt:formatNumber value="${pago.cotizacion}" minFractionDigits="2" maxFractionDigits="2"/></dd></div></dl></div>
