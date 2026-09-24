package ar.com.analitiq.service;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegistroSesionTest {
    @Test void tokensAisladosPorSesionYVencimiento() {
        var uno=new RegistroSesion(); var dos=new RegistroSesion(); var ahora=Instant.now();
        String token=uno.nuevo(ahora);
        assertDoesNotThrow(() -> uno.validar(token,ahora));
        assertThrows(IllegalArgumentException.class,() -> dos.validar(token,ahora));
        assertThrows(IllegalArgumentException.class,() -> uno.validar(token,ahora.plusSeconds(1800)));
        assertNotEquals(uno.getCsrf(),dos.getCsrf());
    }
    @Test void limitaMemoriaSinMezclarPestanas() {
        var s=new RegistroSesion(); var ahora=Instant.now(); String primero=s.nuevo(ahora);
        for(int i=0;i<30;i++) s.nuevo(ahora);
        assertThrows(IllegalArgumentException.class,() -> s.validar(primero,ahora));
    }
}
