package ar.com.analitiq.model;
import java.util.List;
public final class Tratamiento {
    private final long codigo;
    private final String nombre, descripcion;
    private final List<Presupuesto> presupuestos;
    public Tratamiento(long codigo, String nombre, String descripcion, List<Presupuesto> presupuestos) {
        this.codigo = codigo; this.nombre = nombre; this.descripcion = descripcion; this.presupuestos = List.copyOf(presupuestos);
    }
    public long getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public List<Presupuesto> getPresupuestos() { return presupuestos; }
}
