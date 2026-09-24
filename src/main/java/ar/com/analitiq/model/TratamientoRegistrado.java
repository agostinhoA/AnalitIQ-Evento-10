package ar.com.analitiq.model;

public final class TratamientoRegistrado {
    private final long codigo;
    private final Paciente paciente;
    private final TipoTratamiento tipo;
    public TratamientoRegistrado(long codigo,Paciente paciente,TipoTratamiento tipo) {
        this.codigo=codigo; this.paciente=paciente; this.tipo=tipo;
    }
    public long getCodigo() { return codigo; }
    public Paciente getPaciente() { return paciente; }
    public TipoTratamiento getTipo() { return tipo; }
}
