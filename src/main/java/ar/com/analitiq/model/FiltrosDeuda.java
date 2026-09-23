package ar.com.analitiq.model;

import java.io.Serializable;
import java.util.Objects;

public final class FiltrosDeuda implements Serializable {
    private static final long serialVersionUID=1L;
    private final RangoFechas rango;
    private final String tratamiento;
    public FiltrosDeuda(RangoFechas rango, String tratamiento) {
        this.rango=Objects.requireNonNull(rango,"Se requiere un rango validado.");
        this.tratamiento=tratamiento==null ? "" : tratamiento.strip();
        if(this.tratamiento.length()>30)
            throw new IllegalArgumentException("El nombre del tratamiento admite hasta 30 caracteres.");
    }
    public RangoFechas getRango() { return rango; }
    public String getTratamiento() { return tratamiento; }
}
