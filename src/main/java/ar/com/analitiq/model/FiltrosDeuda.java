package ar.com.analitiq.model;

import java.io.Serializable;
import java.util.Objects;

public final class FiltrosDeuda implements Serializable {
    private static final long serialVersionUID=1L;
    private final RangoFechas rango;
    private final String tratamiento;
    private final java.time.LocalDate fechaCorte;
    public FiltrosDeuda(RangoFechas rango, String tratamiento) {
        this(rango,tratamiento,null);
    }
    public FiltrosDeuda(RangoFechas rango, String tratamiento, java.time.LocalDate fechaCorte) {
        this.rango=Objects.requireNonNull(rango,"Se requiere un rango validado.");
        this.fechaCorte=fechaCorte;
        this.tratamiento=tratamiento==null ? "" : tratamiento.strip();
        if(this.tratamiento.length()>30)
            throw new IllegalArgumentException("El nombre del tratamiento admite hasta 30 caracteres.");
    }
    public RangoFechas getRango() { return rango; }
    public java.time.LocalDate getFechaCorte() { return fechaCorte; }
    /** El límite de deuda puede ser anterior al Hasta ingresado. */
    public java.sql.Date getHastaConsultaFecha() {
        var hasta = rango.getHasta();
        if (fechaCorte != null && (hasta == null || fechaCorte.isBefore(hasta))) {
            return java.sql.Date.valueOf(fechaCorte);
        }
        return rango.getHastaFecha();
    }
    public String getTratamiento() { return tratamiento; }
}
