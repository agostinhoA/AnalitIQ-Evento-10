package ar.com.analitiq.service;
import ar.com.analitiq.model.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BusquedaSesionTest {
    private final Instant ahora=Instant.parse("2026-09-18T00:00:00Z");
    private final List<Paciente> homonimos=List.of(new Paciente("30111222","Juan","Pérez"),new Paciente("30999888","Juan","Pérez"));
    private final RangoFechas rango=RangoFechas.parse("2026-09-01","2026-09-30");
    @Test void rechazaDniAjenoYConservaRango() {
        BusquedaSesion s=new BusquedaSesion(); String id=s.agregar(homonimos,rango,ahora);
        assertThrows(IllegalArgumentException.class,()->s.seleccionar(id,"32444555",ahora));
        s.seleccionar(id,"30111222",ahora);
        assertSame(rango,s.obtener(id,ahora).getRango());
        assertEquals("30111222",s.obtener(id,ahora).getDni());
        assertThrows(IllegalArgumentException.class,()->s.seleccionar(id,"30999888",ahora));
    }
    @Test void aisladasPorSesionYPestana() {
        BusquedaSesion a=new BusquedaSesion(), b=new BusquedaSesion();
        String uno=a.agregar(homonimos,rango,ahora);
        String dos=a.agregar(homonimos,RangoFechas.parse("2026-08-01","2026-08-31"),ahora);
        assertThrows(IllegalArgumentException.class,()->b.obtener(uno,ahora));
        a.seleccionar(uno,"30111222",ahora);
        assertNull(a.obtener(dos,ahora).getDni());
    }
    @Test void seleccionUnicaYVencimiento() {
        BusquedaSesion s=new BusquedaSesion(); String id=s.agregar(List.of(homonimos.get(0)),rango,ahora);
        assertEquals("30111222",s.obtener(id,ahora).getDni());
        assertThrows(IllegalArgumentException.class,()->s.obtener(id,ahora.plus(BusquedaSesion.DURACION)));
    }
    @Test void limitaFlujos() {
        BusquedaSesion s=new BusquedaSesion(); String id=s.agregar(homonimos,rango,ahora);
        for(int i=0;i<10;i++) s.agregar(homonimos,rango,ahora);
        assertThrows(IllegalArgumentException.class,()->s.obtener(id,ahora));
    }
    @Test void validacionDeEntradas() {
        assertEquals("30111222",CriterioBusqueda.validar("dni"," 30111222 ","","").dni());
        assertEquals("Lucía",CriterioBusqueda.validar("nombre","","Lucía","Gómez").nombre());
        for(String dni:List.of("1","123456789","' OR 1=1 --","30.111.222"))
            assertThrows(IllegalArgumentException.class,()->CriterioBusqueda.validar("dni",dni,"",""));
        assertThrows(IllegalArgumentException.class,()->CriterioBusqueda.validar("nombre","","Juan",""));
        assertThrows(IllegalArgumentException.class,()->CriterioBusqueda.validar("nombre","","%","Pérez"));
    }
    @Test void filtrarConservaPacienteYNoAlteraOtraPestana() {
        BusquedaSesion s=new BusquedaSesion(); String id=s.agregar(homonimos,rango,ahora);
        assertThrows(IllegalArgumentException.class,()->s.filtrar(id,rango,ahora));
        s.seleccionar(id,"30111222",ahora);
        RangoFechas otro=RangoFechas.parse("2026-08-01","2026-08-31");
        String nuevo=s.filtrar(id,otro,ahora);
        assertNotEquals(id,nuevo);
        assertSame(rango,s.obtener(id,ahora).getRango());
        assertSame(otro,s.obtener(nuevo,ahora).getRango());
        assertEquals("30111222",s.obtener(nuevo,ahora).getDni());
        assertThrows(IllegalArgumentException.class,()->s.filtrar("ajeno",otro,ahora));
    }
}
