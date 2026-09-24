package ar.com.analitiq.dao;

import ar.com.analitiq.model.*;
import ar.com.analitiq.service.DatosTratamiento;
import java.sql.*;
import java.util.*;

public final class RegistroTratamientoDao {
    public List<TipoTratamiento> catalogo(Connection c) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT codigo_tipo_tratamiento,nombre_tratamiento FROM tipos_tratamientos ORDER BY nombre_tratamiento")) {
            s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) {
                var tipos=new ArrayList<TipoTratamiento>();
                while(r.next()) tipos.add(new TipoTratamiento(r.getLong(1),r.getString(2)));
                return List.copyOf(tipos);
            }
        }
    }
    public Paciente paciente(Connection c,String dni) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT dni_paciente,nombre_paciente,apellido_paciente FROM pacientes WHERE dni_paciente=?")) {
            s.setString(1,dni); s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) { return r.next() ? new Paciente(r.getString(1),r.getString(2),r.getString(3)) : null; }
        }
    }
    public TipoTratamiento tipo(Connection c,long codigo) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT nombre_tratamiento FROM tipos_tratamientos WHERE codigo_tipo_tratamiento=?")) {
            s.setLong(1,codigo); s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) { return r.next() ? new TipoTratamiento(codigo,r.getString(1)) : null; }
        }
    }
    public boolean activo(Connection c,String dni,long tipo) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT codigo_tratamiento FROM tratamientos WHERE dni_paciente=? AND codigo_tipo_tratamiento=? AND activo=?")) {
            s.setString(1,dni); s.setLong(2,tipo); s.setString(3,"SI"); s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) { return r.next(); }
        }
    }
    public TratamientoRegistrado porToken(Connection c,String token) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("""
            SELECT t.codigo_tratamiento,p.dni_paciente,p.nombre_paciente,p.apellido_paciente,
                   tt.codigo_tipo_tratamiento,tt.nombre_tratamiento
            FROM tratamientos t JOIN pacientes p ON p.dni_paciente=t.dni_paciente
            JOIN tipos_tratamientos tt ON tt.codigo_tipo_tratamiento=t.codigo_tipo_tratamiento
            WHERE t.token_registro=?
            """)) {
            s.setString(1,token); s.setQueryTimeout(10);
            try(ResultSet r=s.executeQuery()) {
                return r.next() ? new TratamientoRegistrado(r.getLong(1),new Paciente(r.getString(2),r.getString(3),r.getString(4)),new TipoTratamiento(r.getLong(5),r.getString(6))) : null;
            }
        }
    }
    public long insertar(Connection c,DatosTratamiento d,String token) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("""
            INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,
                fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo,token_registro)
            VALUES (?,?,?,?,?,?,?,?)
            """,Statement.RETURN_GENERATED_KEYS)) {
            s.setLong(1,d.tipo()); s.setString(2,d.dni()); s.setDate(3,java.sql.Date.valueOf(d.inicio()));
            s.setDate(4,d.fin()==null ? null : java.sql.Date.valueOf(d.fin()));
            s.setString(5,d.objetivos()); s.setString(6,d.pronostico()); s.setString(7,"SI"); s.setString(8,token);
            s.setQueryTimeout(10); s.executeUpdate();
            try(ResultSet r=s.getGeneratedKeys()) {
                if(!r.next()) throw new SQLException("Falta clave generada");
                return r.getLong(1);
            }
        }
    }
}
