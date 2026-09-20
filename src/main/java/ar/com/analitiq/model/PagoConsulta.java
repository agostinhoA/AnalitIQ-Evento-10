package ar.com.analitiq.model;
public final class PagoConsulta {
    private final long codigo;
    private final Pago pago;
    public PagoConsulta(long codigo, Pago pago) { this.codigo = codigo; this.pago = pago; }
    public long getCodigo() { return codigo; }
    public Pago getPago() { return pago; }
}
