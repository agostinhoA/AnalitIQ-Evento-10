package ar.com.analitiq.model;
import java.util.List;
public final class Informe {
    private final Paciente paciente;
    private final TipoConsulta tipo;
    private final List<PagoConsulta> consultas;
    private final List<Tratamiento> tratamientos;
    public Informe(Paciente paciente, TipoConsulta tipo, List<PagoConsulta> consultas, List<Tratamiento> tratamientos) {
        this.paciente = paciente; this.tipo = tipo; this.consultas = List.copyOf(consultas); this.tratamientos = List.copyOf(tratamientos);
    }
    public Paciente getPaciente() { return paciente; }
    public TipoConsulta getTipo() { return tipo; }
    public List<PagoConsulta> getConsultas() { return consultas; }
    public List<Tratamiento> getTratamientos() { return tratamientos; }
}
