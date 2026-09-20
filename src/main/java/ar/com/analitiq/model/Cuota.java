package ar.com.analitiq.model;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
public final class Cuota {
    private final long codigo;
    private final int numero;
    private final BigDecimal monto;
    private final String estado;
    private final Date vencimiento;
    private final List<Pago> pagos;
    public Cuota(long codigo, int numero, BigDecimal monto, String estado, Date vencimiento, List<Pago> pagos) {
        this.codigo = codigo; this.numero = numero; this.monto = monto; this.estado = estado;
        this.vencimiento = vencimiento; this.pagos = List.copyOf(pagos);
    }
    public long getCodigo() { return codigo; }
    public int getNumero() { return numero; }
    public BigDecimal getMonto() { return monto; }
    public String getEstado() { return estado; }
    public Date getVencimiento() { return vencimiento; }
    public List<Pago> getPagos() { return pagos; }
}
