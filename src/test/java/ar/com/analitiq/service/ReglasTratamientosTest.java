package ar.com.analitiq.service;

import ar.com.analitiq.model.TratamientoActivo;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReglasTratamientosTest {
    private TratamientoActivo t(long codigo,long tipo) {
        return new TratamientoActivo(codigo,tipo,"Tipo " + tipo,null,null,null,null,null);
    }
    @Test void permiteCeroYHastaTresTiposDistintos() {
        assertTrue(ReglasTratamientos.inconsistencias(List.of()).isEmpty());
        assertTrue(ReglasTratamientos.inconsistencias(List.of(t(1,1),t(2,2),t(3,3))).isEmpty());
    }
    @Test void informaTipoRepetidoSinOcultarRegistros() {
        var datos=List.of(t(1,1),t(2,1));
        assertEquals(1,ReglasTratamientos.inconsistencias(datos).size());
        assertEquals(2,datos.size());
    }
    @Test void informaMaximoYRepeticionIndependientemente() {
        var avisos=ReglasTratamientos.inconsistencias(List.of(t(1,1),t(2,2),t(3,3),t(4,1)));
        assertEquals(2,avisos.size());
        assertTrue(avisos.get(0).contains("4 tratamientos activos"));
    }
}
