package ar.com.analitiq.dao;

import ar.com.analitiq.model.*;
import java.sql.*;
import java.util.*;

/** Consultas de sólo lectura. Todos los criterios variables son parámetros JDBC. */
public final class PagosDao {
    private static final String PAGO = "p.codigo_pago,p.monto_pagado,p.fecha_de_pago,m.nombre_metodo,mo.nombre_moneda,p.cotizacion_aplicada ";
    private static final String CATALOGOS = " JOIN metodos_pagos m ON m.codigo_metodo_pago=p.codigo_metodo_pago JOIN tipos_monedas mo ON mo.codigo_tipo_moneda=p.codigo_tipo_moneda ";
    private Pago pago(ResultSet r) throws SQLException {
        return new Pago(r.getLong("codigo_pago"),r.getBigDecimal("monto_pagado"),r.getDate("fecha_de_pago"),r.getString("nombre_metodo"),r.getString("nombre_moneda"),r.getBigDecimal("cotizacion_aplicada"));
    }
    public List<PagoConsulta> consultas(Connection c, String dni) throws SQLException {
        String sql = "SELECT co.codigo_consulta," + PAGO + "FROM consultas co JOIN turnos tu ON tu.codigo_turno=co.codigo_turno JOIN pagos p ON p.codigo_pago=co.codigo_pago" + CATALOGOS + "WHERE tu.dni_paciente=? ORDER BY p.fecha_de_pago DESC,co.codigo_consulta";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1,dni); s.setQueryTimeout(10);
            try (ResultSet r=s.executeQuery()) {
                List<PagoConsulta> result=new ArrayList<>();
                while(r.next()) result.add(new PagoConsulta(r.getLong("codigo_consulta"),pago(r)));
                return result;
            }
        }
    }
    public List<Tratamiento> tratamientos(Connection c,String dni) throws SQLException {
        try (PreparedStatement s=c.prepareStatement("SELECT t.codigo_tratamiento,tt.nombre_tratamiento,tt.descripcion_general FROM tratamientos t JOIN tipos_tratamientos tt ON tt.codigo_tipo_tratamiento=t.codigo_tipo_tratamiento WHERE t.dni_paciente=? ORDER BY t.codigo_tratamiento")) {
            s.setString(1,dni); s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) {
                List<Tratamiento> result=new ArrayList<>();
                while(r.next()) { long id=r.getLong(1); result.add(new Tratamiento(id,r.getString(2),r.getString(3),presupuestos(c,id))); }
                return result;
            }
        }
    }
    private List<Presupuesto> presupuestos(Connection c,long tratamiento) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT p.codigo_presupuesto,p.monto_total,p.cantidad_cuotas,pe.nombre_periodo_pago FROM presupuestos p JOIN periodos_de_pagos pe ON pe.codigo_periodo_pago=p.codigo_periodo_pago WHERE p.codigo_tratamiento=? ORDER BY p.codigo_presupuesto")) {
            s.setLong(1,tratamiento); s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) {
                List<Presupuesto> result=new ArrayList<>();
                while(r.next()) { long id=r.getLong(1); result.add(new Presupuesto(id,r.getBigDecimal(2),r.getInt(3),r.getString(4),cuotas(c,id))); }
                return result;
            }
        }
    }
    private List<Cuota> cuotas(Connection c,long presupuesto) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT cu.codigo_cuota,cu.nro_cuota,cu.monto_cuota,es.tipo_estado,cu.fecha_vencimiento FROM cuotas cu JOIN estados_cuotas es ON es.codigo_estado_cuota=cu.codigo_estado_cuota WHERE cu.codigo_presupuesto=? ORDER BY cu.nro_cuota,cu.codigo_cuota")) {
            s.setLong(1,presupuesto); s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) {
                List<Cuota> result=new ArrayList<>();
                while(r.next()) { long id=r.getLong(1); result.add(new Cuota(id,r.getInt(2),r.getBigDecimal(3),r.getString(4),r.getDate(5),pagosCuota(c,id))); }
                return result;
            }
        }
    }
    private List<Pago> pagosCuota(Connection c,long cuota) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT " + PAGO + "FROM cuotas_x_pagos cp JOIN pagos p ON p.codigo_pago=cp.codigo_pago" + CATALOGOS + "WHERE cp.codigo_cuota=? ORDER BY p.fecha_de_pago,p.codigo_pago")) {
            s.setLong(1,cuota); s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) {
                List<Pago> result=new ArrayList<>();
                while(r.next()) result.add(pago(r));
                return result;
            }
        }
    }
}
