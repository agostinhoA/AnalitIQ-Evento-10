package ar.com.analitiq.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Límites opcionales e inclusivos. Valores válidos para MySQL DATE. */
public final class RangoFechas implements Serializable {
    private static final long serialVersionUID = 1L;
    private final LocalDate desde, hasta;
    public RangoFechas(LocalDate desde, LocalDate hasta) {
        if ((desde != null && (desde.getYear() < 1000 || desde.getYear() > 9999))
                || (hasta != null && (hasta.getYear() < 1000 || hasta.getYear() > 9999)))
            throw new IllegalArgumentException("Ingresá fechas entre los años 1000 y 9999.");
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new IllegalArgumentException("La fecha Desde no puede ser posterior a Hasta.");
        this.desde = desde; this.hasta = hasta;
    }
    public static RangoFechas parse(String desde, String hasta) {
        try { return new RangoFechas(fecha(desde),fecha(hasta)); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Ingresá fechas válidas para Desde y Hasta."); }
    }
    private static LocalDate fecha(String valor) {
        if (valor == null || valor.isBlank()) return null;
        if (!valor.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
            throw new IllegalArgumentException("Ingresá fechas válidas con formato año-mes-día.");
        return LocalDate.parse(valor);
    }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public boolean isSinLimites() { return desde == null && hasta == null; }
    public java.sql.Date getDesdeFecha() { return desde == null ? null : java.sql.Date.valueOf(desde); }
    public java.sql.Date getHastaFecha() { return hasta == null ? null : java.sql.Date.valueOf(hasta); }
}
