package ar.com.analitiq.service;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/** Tokens por formulario y sesión, independientes de las búsquedas del evento 10. */
public final class RegistroSesion implements Serializable {
    private final String csrf=UUID.randomUUID().toString();
    private final Map<String,Instant> formularios=new LinkedHashMap<>();
    public String getCsrf() { return csrf; }
    public synchronized String nuevo(Instant ahora) {
        purgar(ahora);
        while(formularios.size()>=30) formularios.remove(formularios.keySet().iterator().next());
        String token=UUID.randomUUID().toString(); formularios.put(token,ahora); return token;
    }
    public synchronized void validar(String token,Instant ahora) {
        purgar(ahora);
        if(!formularios.containsKey(token)) throw new IllegalArgumentException("El formulario venció o pertenece a otra sesión. Abrí un nuevo registro.");
    }
    private void purgar(Instant ahora) { formularios.entrySet().removeIf(e -> !e.getValue().plusSeconds(1800).isAfter(ahora)); }
}
