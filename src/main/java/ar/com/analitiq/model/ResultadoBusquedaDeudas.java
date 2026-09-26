package ar.com.analitiq.model;

import java.util.*;

/** Pacientes únicos para la selección y grupos por tratamiento para presentarlos. */
public final class ResultadoBusquedaDeudas {
    private final List<Paciente> pacientes;
    private final Map<String,List<Paciente>> grupos;

    public ResultadoBusquedaDeudas(Map<String,List<Paciente>> grupos) {
        Map<String,List<Paciente>> copia=new LinkedHashMap<>();
        Map<String,Paciente> unicos=new LinkedHashMap<>();
        grupos.forEach((tratamiento,miembros) -> {
            List<Paciente> lista=List.copyOf(miembros);
            copia.put(tratamiento,lista);
            lista.forEach(p -> unicos.putIfAbsent(p.getDni(),p));
        });
        this.grupos=Collections.unmodifiableMap(copia);
        this.pacientes=unicos.values().stream()
            .sorted(Comparator.comparing(Paciente::getApellido)
                .thenComparing(Paciente::getNombre).thenComparing(Paciente::getDni))
            .toList();
    }
    public List<Paciente> getPacientes() { return pacientes; }
    public Map<String,List<Paciente>> getGrupos() { return grupos; }
}
