package ar.com.analitiq.service;

import ar.com.analitiq.config.Database;
import ar.com.analitiq.model.*;
import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class DeudasMySqlTest {
    private HikariDataSource ds;
    private DeudasService s;
    @BeforeEach void abrir() throws Exception {
        assumeTrue(System.getenv("ANALITIQ_TEST_DEUDAS")!=null,"Requiere MySQL demo y scripts 04/05.");
        ds=Database.open(); s=new DeudasService(ds);
    }
    @AfterEach void cerrar() { if(ds!=null) ds.close(); }
    private FiltrosDeuda filtro(String desde,String hasta,String tratamiento) {
        return new FiltrosDeuda(RangoFechas.parse(desde,hasta),tratamiento);
    }
    private List<Long> cuotas(InformeDeudas i) {
        return i.getTratamientos().stream().flatMap(t -> t.getCuotas().stream()).map(Cuota::getCodigo).toList();
    }
    @Test void soloBloquesConDeudasSinCortePorHoyYTotalesCorrectos() throws Exception {
        var i=s.consultar("45000001",filtro("2026-01-01","2027-12-31",""));
        assertEquals(List.of(2101L,2102L),i.getTratamientos().stream().map(t -> t.getTratamiento().getCodigo()).toList());
        assertEquals(List.of(2301L,2302L,2303L,2304L,2305L,2308L),cuotas(i));
        assertEquals(6,cuotas(i).stream().distinct().count());
        assertEquals(5,i.getTratamientos().get(0).getCantidad());
        assertEquals(new BigDecimal("50000.00"),i.getTratamientos().get(0).getTotal());
        assertEquals(1,i.getTratamientos().get(1).getCantidad());
        assertEquals(new BigDecimal("20000.00"),i.getTratamientos().get(1).getTotal());
        assertTrue(i.getAvisos().isEmpty());
    }
    @Test void incluyeAmbosLimitesYUnSoloDia() throws Exception {
        assertEquals(List.of(2302L,2303L,2304L),cuotas(s.consultar("45000001",filtro("2026-09-01","2026-09-30",""))));
        assertEquals(List.of(2304L),cuotas(s.consultar("45000001",filtro("2026-09-30","2026-09-30",""))));
    }
    @Test void tratamientoExactoActivoDelPacienteYParametrosLiterales() throws Exception {
        for(String nombre:List.of(" Implante ","implante","IMPLANTE","iMpLaNtE"))
            assertEquals(List.of(2308L),cuotas(s.consultar("45000001",filtro("2026-01-01","2027-12-31",nombre))));
        for(String nombre:List.of("ortodoncia","Ortodoncia","ORTODONCIA"))
            assertEquals(List.of(2302L,2303L),cuotas(s.consultar("45000001",filtro("2026-09-01","2026-09-15",nombre))));
        for(String nombre:List.of("Conducto","No existe","%' OR 1=1 --"))
            assertTrue(s.consultar("45000001",filtro("2026-01-01","2027-12-31",nombre)).getTratamientos().isEmpty());
        assertTrue(s.consultar("45000002",filtro("2026-01-01","2027-12-31","Conducto")).getTratamientos().isEmpty());
    }
    @Test void sinDeudasConservaIdentificacionYRango() throws Exception {
        var filtros=filtro("2026-01-01","2027-12-31","");
        for(String dni:List.of("45000002","40123456","37888999","30999888")) {
            var i=s.consultar(dni,filtros);
            assertEquals(dni,i.getPaciente().getDni());
            assertSame(filtros.getRango(),i.getRango());
            assertTrue(i.getTratamientos().isEmpty());
        }
    }
    @Test void datosRegeneradosNoTienenActivosDuplicados() throws Exception {
        var filtros=filtro("2026-01-01","2027-12-31","Implante");
        for(var paciente:s.buscar(CriterioBusqueda.validarEntrada("","","")))
            assertTrue(s.consultar(paciente.getDni(),filtros).getAvisos().isEmpty());
    }
    @Test void listaCompletaYBusquedasReales() throws Exception {
        assertEquals(10,s.buscar(CriterioBusqueda.validarEntrada("","","")).size());
        assertEquals(1,s.buscar(CriterioBusqueda.validarEntrada("45000001","","")).size());
        assertEquals(1,s.buscar(CriterioBusqueda.validarEntrada("","Lucía","Gómez")).size());
        assertEquals(2,s.buscar(CriterioBusqueda.validarEntrada("","Juan","Pérez")).size());
        assertTrue(s.buscar(CriterioBusqueda.validarEntrada("99999999","","")).isEmpty());
    }
}
