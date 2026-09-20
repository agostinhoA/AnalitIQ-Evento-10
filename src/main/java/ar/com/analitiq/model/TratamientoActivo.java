package ar.com.analitiq.model;

import java.sql.Date;

/** Datos del tratamiento tal como están registrados, sin inferir activo por fechas. */
public final class TratamientoActivo {
    private final long codigo, codigoTipo;
    private final String nombre, descripcion, objetivos, pronostico;
    private final Date inicio, finalizacionEstimada;
    public TratamientoActivo(long codigo, long codigoTipo, String nombre, String descripcion,
            Date inicio, Date finalizacionEstimada, String objetivos, String pronostico) {
        this.codigo = codigo; this.codigoTipo = codigoTipo; this.nombre = nombre;
        this.descripcion = descripcion; this.inicio = inicio; this.finalizacionEstimada = finalizacionEstimada;
        this.objetivos = objetivos; this.pronostico = pronostico;
    }
    public long getCodigo() { return codigo; }
    public long getCodigoTipo() { return codigoTipo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Date getInicio() { return inicio; }
    public Date getFinalizacionEstimada() { return finalizacionEstimada; }
    public String getObjetivos() { return objetivos; }
    public String getPronostico() { return pronostico; }
}
