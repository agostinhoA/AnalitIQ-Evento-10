package ar.com.analitiq.dao;

import ar.com.analitiq.model.*;
import java.sql.*;
import java.util.*;

public final class DeudasDao {
    /** Sin JOIN a pagos: una fila por cuota, con saldo completo y sin corte por hoy. */
    public Map<Long,List<Cuota>> cuotasConDeuda(Connection c,String dni,FiltrosDeuda filtros) throws SQLException {
        String sql="""
            SELECT t.codigo_tratamiento,cu.codigo_cuota,cu.nro_cuota,cu.monto_cuota,
                   ec.tipo_estado,cu.fecha_vencimiento
            FROM tratamientos t
            JOIN tipos_tratamientos tt ON tt.codigo_tipo_tratamiento=t.codigo_tipo_tratamiento
            JOIN presupuestos p ON p.codigo_tratamiento=t.codigo_tratamiento
            JOIN cuotas cu ON cu.codigo_presupuesto=p.codigo_presupuesto
            JOIN estados_cuotas ec ON ec.codigo_estado_cuota=cu.codigo_estado_cuota
            WHERE t.dni_paciente=? AND t.activo=? AND ec.tipo_estado=?
              AND cu.fecha_vencimiento>=? AND cu.fecha_vencimiento<=?
            """;
        boolean porNombre=!filtros.getTratamiento().isEmpty();
        if(porNombre) sql+=" AND tt.nombre_tratamiento=? ";
        sql+=" ORDER BY t.codigo_tratamiento,cu.fecha_vencimiento,cu.nro_cuota,cu.codigo_cuota";
        Map<Long,List<Cuota>> resultado=new LinkedHashMap<>();
        try(PreparedStatement s=c.prepareStatement(sql)) {
            s.setString(1,dni); s.setString(2,"SI"); s.setString(3,"Adeuda");
            s.setDate(4,filtros.getRango().getDesdeFecha()); s.setDate(5,filtros.getRango().getHastaFecha());
            if(porNombre) s.setString(6,filtros.getTratamiento());
            s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) {
                while(r.next()) resultado.computeIfAbsent(r.getLong(1),k -> new ArrayList<>())
                    .add(new Cuota(r.getLong(2),r.getInt(3),r.getBigDecimal(4),r.getString(5),r.getDate(6),List.of()));
            }
        }
        return resultado;
    }

    /** No LIMIT: las inconsistencias deben ser visibles, nunca ocultarse. */
    public List<TratamientoActivo> activos(Connection c, String dni) throws SQLException {
        String sql = """
            SELECT t.codigo_tratamiento,t.codigo_tipo_tratamiento,tt.nombre_tratamiento,
                   tt.descripcion_general,t.fecha_inicio_tratamiento,
                   t.fecha_finalizacion_tratamiento_estimada,t.objetivos,t.pronostico
            FROM tratamientos t
            JOIN tipos_tratamientos tt ON tt.codigo_tipo_tratamiento=t.codigo_tipo_tratamiento
            WHERE t.dni_paciente=? AND t.activo=?
            ORDER BY t.codigo_tipo_tratamiento,t.codigo_tratamiento
            """;
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1,dni); s.setString(2,"SI"); s.setQueryTimeout(10);
            try (ResultSet r = s.executeQuery()) {
                List<TratamientoActivo> result = new ArrayList<>();
                while (r.next()) result.add(new TratamientoActivo(r.getLong(1),r.getLong(2),r.getString(3),
                    r.getString(4),r.getDate(5),r.getDate(6),r.getString(7),r.getString(8)));
                return List.copyOf(result);
            }
        }
    }
}
