package ar.com.analitiq.model;
import java.math.BigDecimal;
import java.util.List;
public final class Presupuesto {
    private final long codigo;
    private final BigDecimal monto;
    private final int cantidadCuotas;
    private final String periodo;
    private final List<Cuota> cuotas;
    public Presupuesto(long codigo, BigDecimal monto, int cantidadCuotas, String periodo, List<Cuota> cuotas) {
        this.codigo = codigo; this.monto = monto; this.cantidadCuotas = cantidadCuotas; this.periodo = periodo; this.cuotas = List.copyOf(cuotas);
    }
    public long getCodigo() { return codigo; }
    public BigDecimal getMonto() { return monto; }
    public int getCantidadCuotas() { return cantidadCuotas; }
    public String getPeriodo() { return periodo; }
    public List<Cuota> getCuotas() { return cuotas; }
}
