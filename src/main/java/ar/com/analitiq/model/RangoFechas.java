package ar.com.analitiq.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Ambos límites son obligatorios e inclusivos. Valores válidos para MySQL DATE. */
public final class RangoFechas implements Serializable {
    private static final long serialVersionUID = 1L;
    private final LocalDate desde, hasta;
    public RangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) throw new IllegalArgumentException("Ingresá las fechas Desde y Hasta.");
        if (desde.getYear() < 1000 || hasta.getYear() > 9999)
            throw new IllegalArgumentException("Ingresá fechas entre los años 1000 y 9999.");
        if (desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha Desde no puede ser posterior a Hasta.");
        this.desde = desde; this.hasta = hasta;
    }
    public static RangoFechas parse(String desde, String hasta) {
        if (desde == null || hasta == null || desde.isBlank() || hasta.isBlank())
            throw new IllegalArgumentException("Ingresá las fechas Desde y Hasta.");
        if (!desde.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}") || !hasta.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
            throw new IllegalArgumentException("Ingresá fechas válidas con formato año-mes-día.");
        try { return new RangoFechas(LocalDate.parse(desde),LocalDate.parse(hasta)); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Ingresá fechas válidas para Desde y Hasta."); }
    }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public java.sql.Date getDesdeFecha() { return java.sql.Date.valueOf(desde); }
    public java.sql.Date getHastaFecha() { return java.sql.Date.valueOf(hasta); }
}
