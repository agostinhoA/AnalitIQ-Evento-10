package ar.com.analitiq.dao;
import ar.com.analitiq.model.Paciente;
import ar.com.analitiq.model.FiltrosDeuda;
import ar.com.analitiq.model.ResultadoBusquedaDeudas;
import ar.com.analitiq.service.CriterioBusqueda;
import java.sql.*;
import java.util.*;

public final class PacienteDao {
    public List<Paciente> buscarConDeudas(Connection c,CriterioBusqueda criterio,FiltrosDeuda filtros) throws SQLException {
        return buscarAgrupadoConDeudas(c,criterio,filtros).getPacientes();
    }
    public ResultadoBusquedaDeudas buscarAgrupadoConDeudas(Connection c,CriterioBusqueda criterio,FiltrosDeuda filtros) throws SQLException {
        String sql="""
            SELECT DISTINCT tt.nombre_tratamiento,p.dni_paciente,p.nombre_paciente,p.apellido_paciente
            FROM pacientes p
            JOIN tratamientos t ON t.dni_paciente=p.dni_paciente
            JOIN tipos_tratamientos tt ON tt.codigo_tipo_tratamiento=t.codigo_tipo_tratamiento
            JOIN presupuestos pr ON pr.codigo_tratamiento=t.codigo_tratamiento
            JOIN cuotas cu ON cu.codigo_presupuesto=pr.codigo_presupuesto
            JOIN estados_cuotas ec ON ec.codigo_estado_cuota=cu.codigo_estado_cuota
            WHERE t.activo='SI' AND ec.tipo_estado='Adeuda'
            """;
        if(!criterio.esListado()) sql+=criterio.dni()!=null ? " AND p.dni_paciente=? " : " AND p.nombre_paciente=? AND p.apellido_paciente=? ";
        if(filtros.getRango().getDesde()!=null) sql+=" AND cu.fecha_vencimiento>=? ";
        if(filtros.getRango().getHasta()!=null) sql+=" AND cu.fecha_vencimiento<=? ";
        if(!filtros.getTratamiento().isEmpty()) sql+=" AND LOWER(tt.nombre_tratamiento)=LOWER(?) ";
        sql+=" ORDER BY tt.nombre_tratamiento,p.apellido_paciente,p.nombre_paciente,p.dni_paciente";
        try(PreparedStatement s=c.prepareStatement(sql)) {
            int index=1;
            if(criterio.dni()!=null) s.setString(index++,criterio.dni());
            else if(!criterio.esListado()) { s.setString(index++,criterio.nombre()); s.setString(index++,criterio.apellido()); }
            if(filtros.getRango().getDesde()!=null) s.setDate(index++,filtros.getRango().getDesdeFecha());
            if(filtros.getRango().getHasta()!=null) s.setDate(index++,filtros.getRango().getHastaFecha());
            if(!filtros.getTratamiento().isEmpty()) s.setString(index,filtros.getTratamiento());
            s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) {
                Map<String,List<Paciente>> grupos=new LinkedHashMap<>();
                while(r.next()) grupos.computeIfAbsent(r.getString(1),k -> new ArrayList<>())
                    .add(new Paciente(r.getString(2),r.getString(3),r.getString(4)));
                return new ResultadoBusquedaDeudas(grupos);
            }
        }
    }
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
