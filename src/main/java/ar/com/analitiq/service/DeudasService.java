package ar.com.analitiq.service;

import ar.com.analitiq.dao.*;
import ar.com.analitiq.model.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public final class DeudasService {
    private final DataSource ds;
    private final PacienteDao pacientes=new PacienteDao();
    private final DeudasDao deudas=new DeudasDao();
    private final RegistroTratamientoDao tratamientos=new RegistroTratamientoDao();
    public DeudasService(DataSource ds) { this.ds=ds; }
    public List<TipoTratamiento> catalogo() throws SQLException {
        try(Connection c=ds.getConnection()) { return tratamientos.catalogo(c); }
    }
    public FiltrosDeuda validarFiltro(FiltrosDeuda filtros) throws SQLException {
        if(filtros.getTratamiento().isEmpty()) return filtros;
        return catalogo().stream()
            .filter(tipo -> tipo.getNombre().equalsIgnoreCase(filtros.getTratamiento()))
            .findFirst()
            .map(tipo -> new FiltrosDeuda(filtros.getRango(),tipo.getNombre()))
            .orElseThrow(() -> new IllegalArgumentException("Elegí un tratamiento del catálogo."));
    }
    public List<Paciente> buscar(CriterioBusqueda criterio) throws SQLException {
        try(Connection c=ds.getConnection()) { return pacientes.buscar(c,criterio); }
    }
    public List<Paciente> buscar(CriterioBusqueda criterio,FiltrosDeuda filtros) throws SQLException {
        Objects.requireNonNull(filtros,"Se requieren filtros validados.");
        try(Connection c=ds.getConnection()) { return pacientes.buscarConDeudas(c,criterio,filtros); }
    }
    public InformeDeudas consultar(String dni, FiltrosDeuda filtros) throws SQLException {
        Objects.requireNonNull(filtros,"Se requieren filtros validados.");
        try(Connection c=ds.getConnection()) {
            c.setReadOnly(true); c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            c.setAutoCommit(false);
            try {
                Paciente paciente=pacientes.porDni(c,dni);
                if(paciente==null) throw new IllegalArgumentException("El paciente ya no está disponible. Iniciá una nueva búsqueda.");
                var activos=deudas.activos(c,dni);
                var porTratamiento=deudas.cuotasConDeuda(c,dni,filtros);
                var bloques=activos.stream().filter(t -> porTratamiento.containsKey(t.getCodigo()))
                    .map(t -> new DeudaTratamiento(t,porTratamiento.get(t.getCodigo()))).toList();
                var informe=new InformeDeudas(paciente,filtros,bloques,ReglasTratamientos.inconsistencias(activos));
                c.commit();
                return informe;
            } catch(SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
}
