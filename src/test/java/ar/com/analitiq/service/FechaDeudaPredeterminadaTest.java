package ar.com.analitiq.service;

import ar.com.analitiq.model.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FechaDeudaPredeterminadaTest {
    private DeudasService servicio(String instante) {
        return new DeudasService(null,Clock.fixed(Instant.parse(instante),ZoneId.of("America/Argentina/Buenos_Aires")));
    }
    @Test void sinFechasUsaElDiaArgentinoYConservaElCorteEnLaSeleccion() throws Exception {
        var vacio=new FiltrosDeuda(RangoFechas.parse("",null),null);
        var efectivo=servicio("2026-11-06T01:00:00Z").validarFiltro(vacio);
        assertNull(efectivo.getRango().getDesde());
        assertNull(efectivo.getRango().getHasta());
        assertEquals(LocalDate.of(2026,11,5),efectivo.getHastaConsultaFecha().toLocalDate());
        assertSame(efectivo,servicio("2026-11-07T15:00:00Z").validarFiltro(efectivo));
        assertTrue(vacio.getRango().isSinLimites());
    }
    @Test void conservaFechasExplicitasIncluidosLimitesUnicos() throws Exception {
        var service=servicio("2026-11-05T15:00:00Z");
        for(var rango:new RangoFechas[]{RangoFechas.parse("2026-12-01",null),
                RangoFechas.parse(null,"2026-12-31"),RangoFechas.parse("2026-09-01","2026-09-30")}) {
            var filtro=new FiltrosDeuda(rango,null);
            var efectivo=service.validarFiltro(filtro);
            assertSame(rango,efectivo.getRango());
            var hastaEsperado=rango.getHasta()==null || rango.getHasta().isAfter(LocalDate.of(2026,11,5))
                ? LocalDate.of(2026,11,5) : rango.getHasta();
            assertEquals(hastaEsperado,efectivo.getHastaConsultaFecha().toLocalDate());
        }
    }
}
