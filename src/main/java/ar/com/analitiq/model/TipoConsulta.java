package ar.com.analitiq.model;

public enum TipoConsulta {
    CONSULTAS("Pagos de consultas"), TRATAMIENTOS("Pagos de tratamientos"), AMBOS("Consultas y tratamientos");
    private final String etiqueta;
    TipoConsulta(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    public boolean isConsultas() { return this != TRATAMIENTOS; }
    public boolean isTratamientos() { return this != CONSULTAS; }
    public static TipoConsulta parse(String value) {
        try { return valueOf(value == null ? "" : value); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Seleccioná un tipo de consulta válido."); }
    }
}
