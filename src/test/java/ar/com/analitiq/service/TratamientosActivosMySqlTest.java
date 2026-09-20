package ar.com.analitiq.service;

import ar.com.analitiq.config.Database;
import ar.com.analitiq.dao.DeudasDao;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class TratamientosActivosMySqlTest {
    @Test void consultaActivosRealesSinLimitarNiMezclarPacientes() throws Exception {
        assumeTrue(System.getenv("ANALITIQ_TEST_DEUDAS")!=null,"Requiere scripts 04 y 05 en MySQL demo.");
        try (HikariDataSource ds=Database.open(); var c=ds.getConnection()) {
            var dao=new DeudasDao();
            var tres=dao.activos(c,"45000001");
            assertEquals(3,tres.size());
            assertEquals(3,tres.stream().map(t -> t.getCodigoTipo()).distinct().count());
            assertTrue(tres.stream().noneMatch(t -> t.getCodigo()==2104));
            assertTrue(dao.activos(c,"45000002").isEmpty());
            var repetidos=dao.activos(c,"45000003");
            assertEquals(2,repetidos.size());
            assertEquals(1,ReglasTratamientos.inconsistencias(repetidos).size());
            var cuatro=dao.activos(c,"45000004");
            assertEquals(4,cuatro.size());
            assertEquals(2,ReglasTratamientos.inconsistencias(cuatro).size());
            assertTrue(dao.activos(c,"' OR 1=1 --").isEmpty());
        }
    }
}
