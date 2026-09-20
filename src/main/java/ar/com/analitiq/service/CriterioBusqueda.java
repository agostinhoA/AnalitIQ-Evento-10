package ar.com.analitiq.service;

public record CriterioBusqueda(String dni, String nombre, String apellido) {
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
