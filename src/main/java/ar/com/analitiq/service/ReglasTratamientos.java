package ar.com.analitiq.service;

import ar.com.analitiq.model.TratamientoActivo;
import java.util.*;

public final class ReglasTratamientos {
    private ReglasTratamientos() {}
    public static List<String> inconsistencias(List<TratamientoActivo> tratamientos) {
        List<String> avisos = new ArrayList<>();
        Map<Long,List<TratamientoActivo>> porTipo = new LinkedHashMap<>();
        for (TratamientoActivo t : tratamientos) porTipo.computeIfAbsent(t.getCodigoTipo(), k -> new ArrayList<>()).add(t);
        for (List<TratamientoActivo> grupo : porTipo.values()) {
            if (grupo.size() > 1) avisos.add("Inconsistencia: hay " + grupo.size() + " tratamientos activos del tipo "
                + grupo.get(0).getNombre() + ". Sólo se permite un tratamiento activo por tipo; se conservan todos los registros.");
        }
        return List.copyOf(avisos);
    }
}
