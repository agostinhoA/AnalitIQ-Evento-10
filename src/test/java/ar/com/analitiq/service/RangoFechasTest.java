package ar.com.analitiq.service;

import ar.com.analitiq.model.RangoFechas;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RangoFechasTest {
    @Test void fechasValidasYOrdenadas() {
        for(String[] valores:new String[][]{{"2026-09-19","2026-09-18"},{"2026-02-29","2026-03-01"},
                {"2026-9-1","2026-09-18"},{"0000-01-01","2026-09-18"},
                {"2026-09-01' OR 1=1 --","2026-09-18"}})
            assertThrows(IllegalArgumentException.class,()->RangoFechas.parse(valores[0],valores[1]));
    }
    @Test void permiteAmbosLimitesVaciosOIndependientes() {
        var sinFechas=RangoFechas.parse(null,"");
        assertTrue(sinFechas.isSinLimites());
        assertNull(sinFechas.getDesdeFecha());
        assertNull(sinFechas.getHastaFecha());
        var desde=RangoFechas.parse("2026-09-01",null);
        assertEquals(LocalDate.of(2026,9,1),desde.getDesde());
        assertNull(desde.getHasta());
        var hasta=RangoFechas.parse("", "2026-09-30");
        assertNull(hasta.getDesde());
        assertEquals(LocalDate.of(2026,9,30),hasta.getHasta());
    }
    @Test void admiteUnDiaYFebreroBisiesto() {
        var unDia=RangoFechas.parse("2026-09-18","2026-09-18");
        assertEquals(unDia.getDesde(),unDia.getHasta());
        assertEquals(LocalDate.of(2024,2,29),RangoFechas.parse("2024-02-29","2024-03-01").getDesde());
    }
}
