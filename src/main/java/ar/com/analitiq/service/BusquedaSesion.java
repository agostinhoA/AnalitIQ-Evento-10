package ar.com.analitiq.service;

import ar.com.analitiq.model.*;
import java.io.Serializable;
import java.time.*;
import java.util.*;

/** Contexto de navegación por sesión; los datos siempre se recuperan de MySQL. */
public final class BusquedaSesion implements Serializable {
    private static final long serialVersionUID=3L;
    private final LinkedHashMap<String,Flujo> flujos=new LinkedHashMap<>();
    public static final Duration DURACION=Duration.ofMinutes(15);
    public synchronized String agregar(List<Paciente> pacientes, FiltrosDeuda filtros,
            CriterioBusqueda criterio, Instant ahora) {
        String dni=!criterio.esListado() && pacientes.size()==1 ? pacientes.get(0).getDni() : null;
        return agregarFlujo(pacientes,Map.of(),filtros,criterio,ahora,dni);
    }
    public synchronized String agregar(ResultadoBusquedaDeudas resultado, FiltrosDeuda filtros,
            CriterioBusqueda criterio, Instant ahora) {
        List<Paciente> pacientes=resultado.getPacientes();
        String dni=!criterio.esListado() && pacientes.size()==1 ? pacientes.get(0).getDni() : null;
        return agregarFlujo(pacientes,resultado.getGrupos(),filtros,criterio,ahora,dni);
    }
    private String agregarFlujo(List<Paciente> pacientes, Map<String,List<Paciente>> grupos, FiltrosDeuda filtros,
            CriterioBusqueda criterio, Instant ahora, String dni) {
        Objects.requireNonNull(filtros); Objects.requireNonNull(criterio);
        flujos.values().removeIf(f -> !ahora.isBefore(f.creado.plus(DURACION)));
        while(flujos.size()>=10) flujos.remove(flujos.keySet().iterator().next());
        String id=UUID.randomUUID().toString();
        flujos.put(id,new Flujo(pacientes,grupos,filtros,criterio,ahora,dni));
        return id;
    }
    public synchronized Flujo obtener(String id, Instant ahora) {
        Flujo f=flujos.get(id);
        if(f==null || !ahora.isBefore(f.creado.plus(DURACION))) {
            flujos.remove(id);
            throw new IllegalArgumentException("La búsqueda venció o no pertenece a esta sesión. Iniciá una nueva búsqueda.");
        }
        return f;
    }
    public synchronized String seleccionar(String id, String dni, Instant ahora) {
        Flujo f=obtener(id,ahora);
        if(dni==null || f.pacientes.stream().noneMatch(p -> p.getDni().equals(dni)))
            throw new IllegalArgumentException("Seleccioná un paciente de los resultados de esta búsqueda.");
        return agregarFlujo(f.pacientes,f.grupos,f.filtros,f.criterio,ahora,dni);
    }
    /** Otro identificador evita modificar informes abiertos en otras pestañas. */
    public synchronized String filtrar(String id, List<Paciente> pacientes, FiltrosDeuda filtros, Instant ahora) {
        Flujo f=obtener(id,ahora);
        if(f.dni==null) throw new IllegalArgumentException("Seleccioná primero un paciente de los resultados.");
        return agregarFlujo(pacientes,Map.of(),filtros,f.criterio,ahora,f.dni);
    }
    public synchronized String filtrar(String id, ResultadoBusquedaDeudas resultado, FiltrosDeuda filtros, Instant ahora) {
        Flujo f=obtener(id,ahora);
        if(f.dni==null) throw new IllegalArgumentException("Seleccioná primero un paciente de los resultados.");
        return agregarFlujo(resultado.getPacientes(),resultado.getGrupos(),filtros,f.criterio,ahora,f.dni);
    }
    public static final class Flujo implements Serializable {
        private static final long serialVersionUID=3L;
        private final List<Paciente> pacientes;
        private final Map<String,List<Paciente>> grupos;
        private final FiltrosDeuda filtros;
        private final CriterioBusqueda criterio;
        private final Instant creado;
        private final String dni;
        private Flujo(List<Paciente> pacientes,Map<String,List<Paciente>> grupos,FiltrosDeuda filtros,CriterioBusqueda criterio,Instant creado,String dni) {
            this.pacientes=List.copyOf(pacientes); this.filtros=filtros;
            Map<String,List<Paciente>> copia=new LinkedHashMap<>();
            grupos.forEach((nombre,miembros) -> copia.put(nombre,List.copyOf(miembros)));
            this.grupos=Collections.unmodifiableMap(copia);
            this.criterio=criterio; this.creado=creado; this.dni=dni;
        }
        public List<Paciente> getPacientes() { return pacientes; }
        public Map<String,List<Paciente>> getGrupos() { return grupos; }
        public FiltrosDeuda getFiltros() { return filtros; }
        public RangoFechas getRango() { return filtros.getRango(); }
        public CriterioBusqueda getCriterio() { return criterio; }
        public boolean isListado() { return criterio.esListado(); }
        public String getDni() { return dni; }
    }
}
