package ar.com.analitiq.service;

import ar.com.analitiq.dao.*;
import ar.com.analitiq.model.*;
import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.*;

public final class DeudasService {
    public static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private final DataSource ds;
    private final Clock clock;
    private final PacienteDao pacientes = new PacienteDao();
    private final DeudasDao deudas = new DeudasDao();
    public DeudasService(DataSource ds) { this(ds,Clock.system(ZONA)); }
    public DeudasService(DataSource ds, Clock clock) { this.ds=ds; this.clock=clock; }
    public List<Paciente> buscar(CriterioBusqueda criterio) throws SQLException {
        try (Connection c=ds.getConnection()) { return pacientes.buscar(c,criterio); }
    }
    public InformeDeudas consultar(String dni, RangoFechas rango) throws SQLException {
        Objects.requireNonNull(rango,"Se requiere un rango validado.");
        LocalDate hoy=LocalDate.now(clock.withZone(ZONA));
        try (Connection c=ds.getConnection()) {
            c.setReadOnly(true); c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            c.setAutoCommit(false);
            try {
                Paciente paciente=pacientes.porDni(c,dni);
                if (paciente==null) throw new IllegalArgumentException("El paciente ya no está disponible. Iniciá una nueva búsqueda.");
                var activos=deudas.activos(c,dni);
                var porTratamiento=deudas.presupuestosConDeudas(c,dni,rango,hoy);
                var bloques=activos.stream().map(t -> new DeudaTratamiento(t,
                    porTratamiento.getOrDefault(t.getCodigo(),List.of()))).toList();
                InformeDeudas informe=new InformeDeudas(paciente,rango,hoy,bloques,ReglasTratamientos.inconsistencias(activos));
                c.commit();
                return informe;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
}
