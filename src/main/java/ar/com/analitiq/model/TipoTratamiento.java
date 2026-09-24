package ar.com.analitiq.model;

public final class TipoTratamiento {
    private final long codigo;
    private final String nombre;
    public TipoTratamiento(long codigo,String nombre) { this.codigo=codigo; this.nombre=nombre; }
    public long getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
}
