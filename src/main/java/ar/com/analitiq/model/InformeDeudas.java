package ar.com.analitiq.model;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public final class InformeDeudas {
    private final Paciente paciente;
    private final RangoFechas rango;
    private final LocalDate fechaCorte;
    private final List<DeudaTratamiento> tratamientos;
    private final List<String> avisos;
    public InformeDeudas(Paciente paciente, RangoFechas rango, LocalDate fechaCorte,
            List<DeudaTratamiento> tratamientos, List<String> avisos) {
        this.paciente=paciente; this.rango=rango; this.fechaCorte=fechaCorte;
        this.tratamientos=List.copyOf(tratamientos); this.avisos=List.copyOf(avisos);
    }
    public Paciente getPaciente() { return paciente; }
    public RangoFechas getRango() { return rango; }
    public Date getFechaCorte() { return Date.valueOf(fechaCorte); }
    public List<DeudaTratamiento> getTratamientos() { return tratamientos; }
    public List<String> getAvisos() { return avisos; }
}
