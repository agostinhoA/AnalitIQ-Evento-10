package ar.com.analitiq.model;

import java.math.BigDecimal;
import java.sql.Date;

public final class Pago {
    private final long codigo;
    private final BigDecimal monto, cotizacion;
    private final Date fecha;
    private final String metodo, moneda;
    public Pago(long codigo, BigDecimal monto, Date fecha, String metodo, String moneda, BigDecimal cotizacion) {
        this.codigo = codigo; this.monto = monto; this.fecha = fecha; this.metodo = metodo; this.moneda = moneda; this.cotizacion = cotizacion;
    }
    public long getCodigo() { return codigo; }
    public BigDecimal getMonto() { return monto; }
    public Date getFecha() { return fecha; }
    public String getMetodo() { return metodo; }
    public String getMoneda() { return moneda; }
    public BigDecimal getCotizacion() { return cotizacion; }
}
