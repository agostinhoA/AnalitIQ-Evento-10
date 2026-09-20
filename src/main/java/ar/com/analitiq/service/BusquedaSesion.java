package ar.com.analitiq.service;

import ar.com.analitiq.model.*;
import java.io.Serializable;
import java.time.*;
import java.util.*;

/** Sólo contexto de navegación; todos los pacientes y cuotas provienen de MySQL. */
public final class BusquedaSesion implements Serializable {
    private static final long serialVersionUID = 2L;
    private final LinkedHashMap<String, Flujo> flujos = new LinkedHashMap<>();
    public static final Duration DURACION = Duration.ofMinutes(15);
    public synchronized String agregar(List<Paciente> pacientes, RangoFechas rango, Instant ahora) {
        return agregarFlujo(pacientes,rango,ahora,pacientes.size()==1 ? pacientes.get(0).getDni() : null);
    }
    private String agregarFlujo(List<Paciente> pacientes, RangoFechas rango, Instant ahora, String dni) {
        Objects.requireNonNull(rango,"Se requiere un rango validado.");
        flujos.values().removeIf(f -> !ahora.isBefore(f.creado.plus(DURACION)));
        while (flujos.size() >= 10) flujos.remove(flujos.keySet().iterator().next());
        String id=UUID.randomUUID().toString();
        flujos.put(id,new Flujo(pacientes,rango,ahora,dni));
        return id;
    }
    public synchronized Flujo obtener(String id, Instant ahora) {
        Flujo flujo=flujos.get(id);
        if (flujo==null || !ahora.isBefore(flujo.creado.plus(DURACION))) {
            flujos.remove(id);
            throw new IllegalArgumentException("La búsqueda venció o no pertenece a esta sesión. Iniciá una nueva búsqueda.");
        }
        return flujo;
    }
    public synchronized void seleccionar(String id, String dni, Instant ahora) {
        Flujo f=obtener(id,ahora);
        if (dni==null || f.pacientes.stream().noneMatch(p -> p.getDni().equals(dni)))
            throw new IllegalArgumentException("Seleccioná un paciente de los resultados de esta búsqueda.");
        // Una selección confirmada no puede cambiar desde otra solicitud concurrente.
        if (f.dni != null && !f.dni.equals(dni))
            throw new IllegalArgumentException("Esta búsqueda ya tiene un paciente seleccionado. Iniciá otra búsqueda.");
        flujos.put(id,new Flujo(f.pacientes,f.rango,f.creado,dni));
    }
    /** Cambiar el rango crea otro flujo para no alterar informes abiertos en otras pestañas. */
    public synchronized String filtrar(String id, RangoFechas rango, Instant ahora) {
        Flujo f=obtener(id,ahora);
        if (f.dni==null) throw new IllegalArgumentException("Seleccioná primero un paciente de los resultados.");
        return agregarFlujo(f.pacientes,rango,ahora,f.dni);
    }
    public static final class Flujo implements Serializable {
        private static final long serialVersionUID = 2L;
        private final List<Paciente> pacientes;
        private final RangoFechas rango;
        private final Instant creado;
        private final String dni;
        private Flujo(List<Paciente> pacientes, RangoFechas rango, Instant creado, String dni) {
            this.pacientes=List.copyOf(pacientes); this.rango=rango; this.creado=creado; this.dni=dni;
        }
        public List<Paciente> getPacientes() { return pacientes; }
        public RangoFechas getRango() { return rango; }
        public String getDni() { return dni; }
    }
}
