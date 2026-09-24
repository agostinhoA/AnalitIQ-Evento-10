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
            var eva=dao.activos(c,"45000003");
            assertEquals(1,eva.size());
            assertTrue(ReglasTratamientos.inconsistencias(eva).isEmpty());
            var nora=dao.activos(c,"45000004");
            assertEquals(3,nora.size());
            assertTrue(nora.stream().noneMatch(t -> t.getCodigo()==2506));
            assertTrue(ReglasTratamientos.inconsistencias(nora).isEmpty());
            assertTrue(dao.activos(c,"' OR 1=1 --").isEmpty());
        }
    }
}
