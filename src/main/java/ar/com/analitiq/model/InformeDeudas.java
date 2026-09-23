package ar.com.analitiq.model;

import java.util.List;

public final class InformeDeudas {
    private final Paciente paciente;
    private final FiltrosDeuda filtros;
    private final List<DeudaTratamiento> tratamientos;
    private final List<String> avisos;
    public InformeDeudas(Paciente paciente, FiltrosDeuda filtros,
            List<DeudaTratamiento> tratamientos, List<String> avisos) {
        this.paciente=paciente; this.filtros=filtros;
        this.tratamientos=List.copyOf(tratamientos); this.avisos=List.copyOf(avisos);
    }
    public Paciente getPaciente() { return paciente; }
    public RangoFechas getRango() { return filtros.getRango(); }
    public FiltrosDeuda getFiltros() { return filtros; }
    public List<DeudaTratamiento> getTratamientos() { return tratamientos; }
    public List<String> getAvisos() { return avisos; }
}
