package ar.com.analitiq.service;

import ar.com.analitiq.dao.*;
import ar.com.analitiq.model.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

public final class DeudasService {
    private final DataSource ds;
    private final Clock reloj;
    private final PacienteDao pacientes=new PacienteDao();
    private final DeudasDao deudas=new DeudasDao();
    private final RegistroTratamientoDao tratamientos=new RegistroTratamientoDao();
    public DeudasService(DataSource ds) {
        this(ds, Clock.system(ZoneId.of("America/Argentina/Buenos_Aires")));
    }
    DeudasService(DataSource ds, Clock reloj) { this.ds=ds; this.reloj=Objects.requireNonNull(reloj); }
    private FiltrosDeuda fechasEfectivas(FiltrosDeuda filtros) {
        Objects.requireNonNull(filtros,"Se requieren filtros validados.");
        if (filtros.getFechaCorte()!=null) return filtros;
        return new FiltrosDeuda(filtros.getRango(),filtros.getTratamiento(),LocalDate.now(reloj));
    }
    public List<TipoTratamiento> catalogo() throws SQLException {
        try(Connection c=ds.getConnection()) { return tratamientos.catalogo(c); }
    }
    public FiltrosDeuda validarFiltro(FiltrosDeuda filtros) throws SQLException {
        FiltrosDeuda efectivos=fechasEfectivas(filtros);
        if(efectivos.getTratamiento().isEmpty()) return efectivos;
        return catalogo().stream()
            .filter(tipo -> tipo.getNombre().equalsIgnoreCase(filtros.getTratamiento()))
            .findFirst()
            .map(tipo -> new FiltrosDeuda(efectivos.getRango(),tipo.getNombre(),efectivos.getFechaCorte()))
            .orElseThrow(() -> new IllegalArgumentException("Elegí un tratamiento del catálogo."));
    }
    public List<Paciente> buscar(CriterioBusqueda criterio) throws SQLException {
        try(Connection c=ds.getConnection()) { return pacientes.buscar(c,criterio); }
    }
    public List<Paciente> buscar(CriterioBusqueda criterio,FiltrosDeuda filtros) throws SQLException {
        return buscarAgrupado(criterio,filtros).getPacientes();
    }
    public ResultadoBusquedaDeudas buscarAgrupado(CriterioBusqueda criterio,FiltrosDeuda filtros) throws SQLException {
        filtros=fechasEfectivas(filtros);
        try(Connection c=ds.getConnection()) { return pacientes.buscarAgrupadoConDeudas(c,criterio,filtros); }
    }
    public InformeDeudas consultar(String dni, FiltrosDeuda filtros) throws SQLException {
        filtros=fechasEfectivas(filtros);
        try(Connection c=ds.getConnection()) {
            c.setReadOnly(true); c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            c.setAutoCommit(false);
            try {
                Paciente paciente=pacientes.porDni(c,dni);
                if(paciente==null) throw new IllegalArgumentException("El paciente ya no está disponible. Iniciá una nueva búsqueda.");
                var activos=deudas.activos(c,dni);
                var porTratamiento = deudas.cuotasVencidas(c,dni,filtros);
                var bloques = activos.stream()
                    .filter(tratamiento -> porTratamiento.containsKey(tratamiento.getCodigo()))
                    .map(tratamiento -> new DeudaTratamiento(tratamiento, porTratamiento.get(tratamiento.getCodigo())))
                    .toList();
                var informe=new InformeDeudas(paciente,filtros,bloques,ReglasTratamientos.inconsistencias(activos));
                c.commit();
                return informe;
            } catch(SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
}
