package ar.com.analitiq.service;

import ar.com.analitiq.config.Database;
import ar.com.analitiq.model.*;
import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class DeudasMySqlTest {
    private HikariDataSource ds;
    @BeforeEach void abrir() throws Exception {
        assumeTrue(System.getenv("ANALITIQ_TEST_DEUDAS")!=null,"Requiere MySQL demo y scripts 04/05.");
        ds=Database.open();
    }
    @AfterEach void cerrar() { if(ds!=null) ds.close(); }
    private DeudasService servicio(String hoy) {
        return new DeudasService(ds,Clock.fixed(LocalDate.parse(hoy).atStartOfDay(DeudasService.ZONA).toInstant(),DeudasService.ZONA));
    }
    private List<Long> cuotas(InformeDeudas i) {
        return i.getTratamientos().stream().flatMap(t -> t.getPresupuestos().stream())
            .flatMap(p -> p.getCuotas().stream()).map(Cuota::getCodigo).toList();
    }
    @Test void agrupaTresActivosYExcluyePagadasFuturasEInactivos() throws Exception {
        var i=servicio("2026-09-18").consultar("45000001",RangoFechas.parse("2026-09-01","2026-09-30"));
        assertEquals(3,i.getTratamientos().size());
        assertEquals(List.of(2302L,2303L),cuotas(i));
        assertTrue(i.getTratamientos().get(1).getPresupuestos().isEmpty());
        assertTrue(i.getTratamientos().get(2).getPresupuestos().isEmpty());
        assertEquals(new BigDecimal("10000.00"),i.getTratamientos().get(0).getPresupuestos().get(0).getCuotas().get(0).getMonto());
        assertTrue(i.getAvisos().isEmpty());
    }
    @Test void incluyeAmbosLimitesYVencimientoDeHoy() throws Exception {
        var s=servicio("2026-09-30");
        assertEquals(List.of(2302L,2303L,2304L),cuotas(s.consultar("45000001",RangoFechas.parse("2026-09-01","2026-09-30"))));
        assertEquals(List.of(2304L),cuotas(s.consultar("45000001",RangoFechas.parse("2026-09-30","2026-09-30"))));
        assertTrue(cuotas(servicio("2026-09-29").consultar("45000001",RangoFechas.parse("2026-09-30","2026-09-30"))).isEmpty());
    }
    @Test void noConfundeSinActivosConActivosSinDeudas() throws Exception {
        var s=servicio("2026-09-18"); var rango=RangoFechas.parse("2026-01-01","2026-12-31");
        assertTrue(s.consultar("45000002",rango).getTratamientos().isEmpty());
        var sinDeuda=s.consultar("40123456",rango);
        assertEquals(1,sinDeuda.getTratamientos().size()); assertTrue(cuotas(sinDeuda).isEmpty());
    }
    @Test void inconsistenciasSeInformanSinOcultarTratamientos() throws Exception {
        var s=servicio("2026-09-18"); var rango=RangoFechas.parse("2026-01-01","2026-12-31");
        var repetidos=s.consultar("45000003",rango); var cuatro=s.consultar("45000004",rango);
        assertEquals(2,repetidos.getTratamientos().size()); assertEquals(1,repetidos.getAvisos().size());
        assertEquals(4,cuatro.getTratamientos().size()); assertEquals(2,cuatro.getAvisos().size());
    }
    @Test void hoySeCalculaEnArgentinaNoEnUtc() throws Exception {
        var rango=RangoFechas.parse("2026-09-15","2026-09-15");
        var antes=new DeudasService(ds,Clock.fixed(Instant.parse("2026-09-15T02:59:00Z"),ZoneOffset.UTC));
        var despues=new DeudasService(ds,Clock.fixed(Instant.parse("2026-09-15T03:00:00Z"),ZoneOffset.UTC));
        assertTrue(cuotas(antes.consultar("45000001",rango)).isEmpty());
        assertEquals(List.of(2303L),cuotas(despues.consultar("45000001",rango)));
    }
}
