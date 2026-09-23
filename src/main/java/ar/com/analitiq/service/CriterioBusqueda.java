package ar.com.analitiq.service;

public record CriterioBusqueda(String dni, String nombre, String apellido) implements java.io.Serializable {
    public boolean esListado() { return dni == null && nombre == null && apellido == null; }
    /** Sin criterio solicita selección explícita de la lista completa. */
    public static CriterioBusqueda validarEntrada(String dni, String nombre, String apellido) {
        dni=limpiar(dni); nombre=limpiar(nombre); apellido=limpiar(apellido);
        if (!dni.isEmpty() && (!nombre.isEmpty() || !apellido.isEmpty()))
            throw new IllegalArgumentException("Ingresá DNI o nombre y apellido, no ambos criterios a la vez.");
        if (!dni.isEmpty()) return validar("dni",dni,"","");
        if (nombre.isEmpty() && apellido.isEmpty()) return new CriterioBusqueda(null,null,null);
        return validar("nombre","",nombre,apellido);
    }
    public static CriterioBusqueda validar(String modo, String dni, String nombre, String apellido) {
        if ("dni".equals(modo)) {
            String value = limpiar(dni);
            if (!value.matches("[0-9]{7,8}")) throw new IllegalArgumentException("Ingresá un DNI de 7 u 8 dígitos, sin puntos.");
            return new CriterioBusqueda(value,null,null);
        }
        if (!"nombre".equals(modo)) throw new IllegalArgumentException("Elegí buscar por DNI o por nombre y apellido.");
        nombre = limpiar(nombre); apellido = limpiar(apellido);
        if (!esNombre(nombre) || !esNombre(apellido))
            throw new IllegalArgumentException("Ingresá nombre y apellido completos, sólo con letras y espacios (máximo 100 caracteres cada uno).");
        return new CriterioBusqueda(null,nombre,apellido);
    }
    private static boolean esNombre(String value) {
        return !value.isEmpty() && value.length() <= 100 && value.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+");
    }
    private static String limpiar(String value) { return value == null ? "" : value.strip(); }
}
