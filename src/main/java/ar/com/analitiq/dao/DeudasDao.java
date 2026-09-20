package ar.com.analitiq.dao;

import ar.com.analitiq.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public final class DeudasDao {
    /** No se une cuotas_x_pagos: cada cuota aparece exactamente una vez. */
    public Map<Long,List<Presupuesto>> presupuestosConDeudas(Connection c, String dni,
            RangoFechas rango, LocalDate hoy) throws SQLException {
        String sql = """
            SELECT t.codigo_tratamiento,p.codigo_presupuesto,p.monto_total,p.cantidad_cuotas,
                   pe.nombre_periodo_pago,cu.codigo_cuota,cu.nro_cuota,cu.monto_cuota,
                   ec.tipo_estado,cu.fecha_vencimiento
            FROM tratamientos t
            JOIN presupuestos p ON p.codigo_tratamiento=t.codigo_tratamiento
            JOIN periodos_de_pagos pe ON pe.codigo_periodo_pago=p.codigo_periodo_pago
            JOIN cuotas cu ON cu.codigo_presupuesto=p.codigo_presupuesto
            JOIN estados_cuotas ec ON ec.codigo_estado_cuota=cu.codigo_estado_cuota
            WHERE t.dni_paciente=? AND t.activo=? AND ec.tipo_estado=?
              AND cu.fecha_vencimiento >= ? AND cu.fecha_vencimiento <= ?
              AND cu.fecha_vencimiento <= ?
            ORDER BY t.codigo_tratamiento,p.codigo_presupuesto,cu.nro_cuota,cu.codigo_cuota
            """;
        Map<Long,Map<Long,PresupuestoFilas>> filas = new LinkedHashMap<>();
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1,dni); s.setString(2,"SI"); s.setString(3,"Adeuda");
            s.setDate(4,rango.getDesdeFecha()); s.setDate(5,rango.getHastaFecha());
            s.setDate(6,java.sql.Date.valueOf(hoy)); s.setQueryTimeout(10);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    var porPresupuesto = filas.computeIfAbsent(r.getLong(1), k -> new LinkedHashMap<>());
                    long codigo = r.getLong(2);
                    PresupuestoFilas presupuesto = porPresupuesto.get(codigo);
                    if (presupuesto == null) {
                        presupuesto = new PresupuestoFilas(codigo,r.getBigDecimal(3),r.getInt(4),r.getString(5));
                        porPresupuesto.put(codigo,presupuesto);
                    }
                    presupuesto.cuotas.add(new Cuota(r.getLong(6),r.getInt(7),r.getBigDecimal(8),
                        r.getString(9),r.getDate(10),List.of()));
                }
            }
        }
        Map<Long,List<Presupuesto>> resultado = new LinkedHashMap<>();
        filas.forEach((tratamiento,presupuestos) -> resultado.put(tratamiento,
            presupuestos.values().stream().map(PresupuestoFilas::modelo).toList()));
        return resultado;
    }

    private static final class PresupuestoFilas {
        final long codigo;
        final BigDecimal monto;
        final int cantidad;
        final String periodo;
        final List<Cuota> cuotas = new ArrayList<>();
        PresupuestoFilas(long codigo, BigDecimal monto, int cantidad, String periodo) {
            this.codigo=codigo; this.monto=monto; this.cantidad=cantidad; this.periodo=periodo;
        }
        Presupuesto modelo() { return new Presupuesto(codigo,monto,cantidad,periodo,cuotas); }
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
