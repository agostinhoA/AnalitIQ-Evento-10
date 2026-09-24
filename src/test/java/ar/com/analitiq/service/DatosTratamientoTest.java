package ar.com.analitiq.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatosTratamientoTest {
    @Test void obligatoriosYOpcionales() {
        var d=DatosTratamiento.parse(" 30999888 ","1","2020-02-29","","  ",null);
        assertEquals("30999888",d.dni()); assertNull(d.fin()); assertNull(d.objetivos()); assertNull(d.pronostico());
        assertDoesNotThrow(() -> DatosTratamiento.parse("30999888","1","2090-01-01","2090-01-01","Objetivos","Pronóstico"));
    }
    @Test void rechazaFormatosYFechasInvalidos() {
        for(String dni:new String[]{"","123","30.999.888","<script>","123456789"})
            assertThrows(IllegalArgumentException.class,() -> DatosTratamiento.parse(dni,"1","2026-01-01","","",""));
        for(String tipo:new String[]{"","0","-1","no","4294967296","1 OR 1=1"})
            assertThrows(IllegalArgumentException.class,() -> DatosTratamiento.parse("30999888",tipo,"2026-01-01","","",""));
        for(String inicio:new String[]{"","2026-02-29","0001-01-01","2026-13-01","2026-1-1"})
            assertThrows(IllegalArgumentException.class,() -> DatosTratamiento.parse("30999888","1",inicio,"","",""));
        assertThrows(IllegalArgumentException.class,() -> DatosTratamiento.parse("30999888","1","2026-09-01","2026-08-31","",""));
    }
    @Test void validaTextEnBytesUtf8() {
        assertDoesNotThrow(() -> DatosTratamiento.parse("30999888","1","2026-01-01","","a".repeat(65535),""));
        assertThrows(IllegalArgumentException.class,() -> DatosTratamiento.parse("30999888","1","2026-01-01","","a".repeat(65536),""));
        assertThrows(IllegalArgumentException.class,() -> DatosTratamiento.parse("30999888","1","2026-01-01","","","🙂".repeat(16384)));
    }
}
