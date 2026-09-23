package ar.com.analitiq.dao;
import ar.com.analitiq.model.Paciente;
import ar.com.analitiq.service.CriterioBusqueda;
import java.sql.*;
import java.util.*;

public final class PacienteDao {
    public List<Paciente> buscar(Connection c, CriterioBusqueda criterio) throws SQLException {
        String sql = "SELECT dni_paciente,nombre_paciente,apellido_paciente FROM pacientes"
            + (criterio.esListado() ? "" : criterio.dni() != null ? " WHERE dni_paciente = ?" : " WHERE nombre_paciente = ? AND apellido_paciente = ?")
            + " ORDER BY apellido_paciente,nombre_paciente,dni_paciente";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setQueryTimeout(10);
            if (criterio.dni() != null) s.setString(1, criterio.dni());
            else if (!criterio.esListado()) { s.setString(1, criterio.nombre()); s.setString(2, criterio.apellido()); }
            try (ResultSet r = s.executeQuery()) {
                List<Paciente> result = new ArrayList<>();
                while (r.next()) result.add(new Paciente(r.getString(1),r.getString(2),r.getString(3)));
                return List.copyOf(result);
            }
        }
    }
    public Paciente porDni(Connection c, String dni) throws SQLException {
        List<Paciente> result = buscar(c, new CriterioBusqueda(dni,null,null));
        return result.isEmpty() ? null : result.get(0);
    }
}
