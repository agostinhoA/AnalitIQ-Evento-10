package ar.com.analitiq.service;

import ar.com.analitiq.dao.*;
import ar.com.analitiq.model.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public final class PagosService {
    private final DataSource ds;
    private final PacienteDao pacientes = new PacienteDao();
    private final PagosDao pagos = new PagosDao();
    public PagosService(DataSource ds) { this.ds = ds; }
    public List<Paciente> buscar(CriterioBusqueda criterio) throws SQLException {
        try (Connection c=ds.getConnection()) { return pacientes.buscar(c,criterio); }
    }
    public Informe consultar(String dni, TipoConsulta tipo) throws SQLException {
        try (Connection c=ds.getConnection()) {
            c.setReadOnly(true);
            c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            c.setAutoCommit(false);
            try {
                Paciente paciente=pacientes.porDni(c,dni);
                if (paciente == null) throw new IllegalArgumentException("El paciente ya no está disponible. Iniciá una nueva búsqueda.");
                Informe informe=new Informe(paciente,tipo,
                    tipo.isConsultas() ? pagos.consultas(c,dni) : List.of(),
                    tipo.isTratamientos() ? pagos.tratamientos(c,dni) : List.of());
                c.commit();
                return informe;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
}
