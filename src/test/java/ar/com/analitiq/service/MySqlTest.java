package ar.com.analitiq.service;
import ar.com.analitiq.config.Database;
import ar.com.analitiq.model.*;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class MySqlTest {
    @Test void consultaDemoRealYPermisos() throws Exception {
        assumeTrue(System.getenv("ANALITIQ_TEST_DB")!=null,"Integración MySQL optativa: ANALITIQ_TEST_DB=1 y ANALITIQ_CONFIG.");
        try(HikariDataSource ds=Database.open()) {
            PagosService s=new PagosService(ds);
            assertEquals(2,s.buscar(CriterioBusqueda.validar("nombre","","Juan","Pérez")).size());
            assertEquals(0,s.buscar(CriterioBusqueda.validar("dni","99999999","","")).size());
            Informe f=s.consultar("30111222",TipoConsulta.AMBOS);
            assertEquals(1,f.getConsultas().size());
            assertEquals(3,f.getTratamientos().get(0).getPresupuestos().get(0).getCuotas().size());
            var cuotas=f.getTratamientos().get(0).getPresupuestos().get(0).getCuotas();
            assertEquals(503,cuotas.get(0).getPagos().get(0).getCodigo());
            assertEquals(503,cuotas.get(1).getPagos().get(0).getCodigo());
            assertTrue(cuotas.get(2).getPagos().isEmpty());
            assertEquals("Adeuda",cuotas.get(2).getEstado());
            assertTrue(s.consultar("30999888",TipoConsulta.AMBOS).getTratamientos().isEmpty());
            assertTrue(s.consultar("35666777",TipoConsulta.AMBOS).getConsultas().isEmpty());
            assertTrue(s.consultar("37888999",TipoConsulta.TRATAMIENTOS).getTratamientos().get(0).getPresupuestos().isEmpty());
            assertTrue(s.consultar("40123456",TipoConsulta.TRATAMIENTOS).getTratamientos().get(0).getPresupuestos().get(0).getCuotas().isEmpty());
            try(Connection c=ds.getConnection()) {
                c.setReadOnly(false); // Comprueba permisos MySQL, no sólo la bandera JDBC.
                try(Statement st=c.createStatement()) {
                    SQLException e=assertThrows(SQLException.class,()->st.executeUpdate("UPDATE pacientes SET nombre_paciente=nombre_paciente WHERE 1=0"));
                    assertEquals(1142,e.getErrorCode());
                }
            }
        }
    }
}
