package ar.com.analitiq.service;

import ar.com.analitiq.dao.RegistroTratamientoDao;
import ar.com.analitiq.model.*;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public final class RegistroTratamientoService {
    private final DataSource ds;
    private final RegistroTratamientoDao dao=new RegistroTratamientoDao();
    public RegistroTratamientoService(DataSource ds) { this.ds=ds; }
    public List<TipoTratamiento> catalogo() throws SQLException {
        try(Connection c=ds.getConnection()) { return dao.catalogo(c); }
    }
    public TratamientoRegistrado resultado(String token) throws SQLException {
        try(Connection c=ds.getConnection()) { return dao.porToken(c,token); }
    }
    public TratamientoRegistrado registrar(DatosTratamiento d,String token) throws SQLException {
        Objects.requireNonNull(d);
        if(token==null || !token.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
            throw new IllegalArgumentException("El formulario venció. Abrí un nuevo registro.");
        try(Connection c=ds.getConnection()) {
            c.setReadOnly(false); c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); c.setAutoCommit(false);
            try {
                // El índice único arbitra las altas simultáneas, incluso desde otras conexiones.
                Paciente paciente=dao.paciente(c,d.dni());
                if(paciente==null) throw new IllegalArgumentException("No existe un paciente registrado con el DNI ingresado");
                var previo=dao.porToken(c,token);
                if(previo!=null) { c.commit(); return previo; }
                TipoTratamiento tipo=dao.tipo(c,d.tipo());
                if(tipo==null) throw new IllegalArgumentException("El tipo de tratamiento seleccionado no existe. Elegí una opción del catálogo.");
                if(dao.activo(c,d.dni(),d.tipo())) throw new IllegalArgumentException("El paciente ya tiene un tratamiento activo de tipo "+tipo.getNombre()+". Sólo se permite uno activo por tipo.");
                long codigo=dao.insertar(c,d,token);
                c.commit();
                return new TratamientoRegistrado(codigo,paciente,tipo);
            } catch(SQLException e) {
                c.rollback();
                if(e.getErrorCode()==1062) {
                    var previo=dao.porToken(c,token);
                    if(previo!=null) return previo;
                    throw new IllegalArgumentException("El paciente ya tiene un tratamiento activo de ese tipo. No se registró otro.");
                }
                throw e;
            } catch(RuntimeException e) { c.rollback(); throw e; }
        }
    }
}
