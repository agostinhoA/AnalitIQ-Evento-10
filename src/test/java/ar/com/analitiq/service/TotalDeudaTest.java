package ar.com.analitiq.service;
import ar.com.analitiq.model.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TotalDeudaTest {
    @Test void totalGeneralSumaSoloLosTratamientosIncluidosEnElInforme() {
        var fecha=Date.valueOf("2027-01-01");
        var primero=new DeudaTratamiento(null,List.of(
            new Cuota(1,1,new BigDecimal("10.10"),"Adeuda",fecha,List.of()),
            new Cuota(2,2,new BigDecimal("20.20"),"Adeuda",fecha,List.of())));
        var segundo=new DeudaTratamiento(null,List.of(
            new Cuota(3,1,new BigDecimal("100.05"),"Adeuda",fecha,List.of())));
        var completo=new InformeDeudas(null,null,List.of(primero,segundo),List.of());
        var filtrado=new InformeDeudas(null,null,List.of(segundo),List.of());
        assertEquals(new BigDecimal("130.35"),completo.getTotalGeneral());
        assertEquals(new BigDecimal("100.05"),filtrado.getTotalGeneral());
    }
    @Test void totalGeneralEsCeroSinDeudas() {
        var informe=new InformeDeudas(null,null,List.of(),List.of());
        assertEquals(BigDecimal.ZERO,informe.getTotalGeneral());
    }
    @Test void sumaDecimalExactaSoloDeLasFilasDelBloque() {
        var fecha=Date.valueOf("2027-01-01");
        var bloque=new DeudaTratamiento(null,List.of(
            new Cuota(1,1,new BigDecimal("10.10"),"Adeuda",fecha,List.of()),
            new Cuota(2,2,new BigDecimal("20.20"),"Adeuda",fecha,List.of())));
        assertEquals(new BigDecimal("30.30"),bloque.getTotal());
        assertEquals(2,bloque.getCantidad());
    }
}
