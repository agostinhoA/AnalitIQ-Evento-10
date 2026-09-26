package ar.com.analitiq.service;
import ar.com.analitiq.model.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BusquedaSesionTest {
    private final Instant ahora=Instant.parse("2026-09-23T00:00:00Z");
    private final List<Paciente> homonimos=List.of(new Paciente("30111222","Juan","Pérez"),new Paciente("30999888","Juan","Pérez"));
    private final FiltrosDeuda filtros=new FiltrosDeuda(RangoFechas.parse("2026-09-01","2026-09-30"),"Ortodoncia");
    private final CriterioBusqueda criterio=CriterioBusqueda.validarEntrada("","Juan","Pérez");
    @Test void rechazaDniAjenoYConservaFiltros() {
        BusquedaSesion s=new BusquedaSesion(); String id=s.agregar(homonimos,filtros,criterio,ahora);
        assertThrows(IllegalArgumentException.class,()->s.seleccionar(id,"32444555",ahora));
        String elegido=s.seleccionar(id,"30111222",ahora);
        assertSame(filtros,s.obtener(elegido,ahora).getFiltros());
        assertNull(s.obtener(id,ahora).getDni());
        assertEquals("30111222",s.obtener(elegido,ahora).getDni());
        String cambiado=s.seleccionar(elegido,"30999888",ahora);
        assertEquals("30999888",s.obtener(cambiado,ahora).getDni());
        assertEquals("30111222",s.obtener(elegido,ahora).getDni());
    }
    @Test void aisladasPorSesionYPestana() {
        BusquedaSesion a=new BusquedaSesion(), b=new BusquedaSesion();
        String uno=a.agregar(homonimos,filtros,criterio,ahora);
        String dos=a.agregar(homonimos,filtros,criterio,ahora);
        assertThrows(IllegalArgumentException.class,()->b.obtener(uno,ahora));
        String elegido=a.seleccionar(uno,"30111222",ahora);
        assertNull(a.obtener(dos,ahora).getDni());
        assertNull(a.obtener(uno,ahora).getDni());
        assertEquals("30111222",a.obtener(elegido,ahora).getDni());
    }
    @Test void seleccionUnicaConCriterioYVencimiento() {
        BusquedaSesion s=new BusquedaSesion(); String id=s.agregar(List.of(homonimos.get(0)),filtros,criterio,ahora);
        assertNull(s.obtener(id,ahora).getDni());
        assertThrows(IllegalArgumentException.class,()->s.obtener(id,ahora.plus(BusquedaSesion.DURACION)));
    }
    @Test void listaCompletaExigeSeleccionInclusoConUnSoloPaciente() {
        BusquedaSesion s=new BusquedaSesion();
        String id=s.agregar(List.of(homonimos.get(0)),filtros,CriterioBusqueda.validarEntrada("","", ""),ahora);
        assertTrue(s.obtener(id,ahora).isListado());
        assertNull(s.obtener(id,ahora).getDni());
        assertThrows(IllegalArgumentException.class,()->s.filtrar(id,homonimos,filtros,ahora));
        String elegido=s.seleccionar(id,"30111222",ahora);
        assertEquals("30111222",s.obtener(elegido,ahora).getDni());
    }
    @Test void limitaFlujosNoPacientes() {
        BusquedaSesion s=new BusquedaSesion(); String id=s.agregar(homonimos,filtros,criterio,ahora);
        for(int i=0;i<10;i++) s.agregar(homonimos,filtros,criterio,ahora);
        assertThrows(IllegalArgumentException.class,()->s.obtener(id,ahora));
    }
    @Test void validacionDeEntradasYConflictos() {
        assertEquals("30111222",CriterioBusqueda.validarEntrada(" 30111222 ","","").dni());
        assertEquals("Lucía",CriterioBusqueda.validarEntrada("","Lucía","Gómez").nombre());
        assertTrue(CriterioBusqueda.validarEntrada("  ",null,"").esListado());
        for(String dni:List.of("1","123456789","' OR 1=1 --","30.111.222"))
            assertThrows(IllegalArgumentException.class,()->CriterioBusqueda.validarEntrada(dni,"",""));
        assertEquals("Juan",CriterioBusqueda.validarEntrada("","Juan","").nombre());
        assertEquals("Pérez",CriterioBusqueda.validarEntrada("","","Pérez").apellido());
        assertThrows(IllegalArgumentException.class,()->CriterioBusqueda.validar("nombre","","",""));
        assertThrows(IllegalArgumentException.class,()->CriterioBusqueda.validarEntrada("","%","Pérez"));
        assertThrows(IllegalArgumentException.class,()->CriterioBusqueda.validarEntrada("30111222","Juan","Pérez"));
        assertThrows(IllegalArgumentException.class,()->CriterioBusqueda.validarEntrada("30111222","","Pérez"));
        assertThrows(IllegalArgumentException.class,()->new FiltrosDeuda(filtros.getRango(),"x".repeat(31)));
    }
    @Test void filtrarConservaPacienteYNoAlteraOtraPestana() {
        BusquedaSesion s=new BusquedaSesion(); String id=s.agregar(homonimos,filtros,criterio,ahora);
        id=s.seleccionar(id,"30111222",ahora);
        var otro=new FiltrosDeuda(RangoFechas.parse("2026-08-01","2026-08-31"),"Implante");
        String nuevo=s.filtrar(id,List.of(homonimos.get(1)),otro,ahora);
        assertNotEquals(id,nuevo);
        assertSame(filtros,s.obtener(id,ahora).getFiltros());
        assertSame(otro,s.obtener(nuevo,ahora).getFiltros());
        assertEquals("30111222",s.obtener(nuevo,ahora).getDni());
        assertEquals(List.of(homonimos.get(1)),s.obtener(nuevo,ahora).getPacientes());
        assertThrows(IllegalArgumentException.class,()->s.filtrar("ajeno",homonimos,otro,ahora));
    }
}
