package ar.com.analitiq.model;

import java.io.Serializable;

public final class Paciente implements Serializable {
    private final String dni, nombre, apellido;
    public Paciente(String dni, String nombre, String apellido) {
        this.dni = dni; this.nombre = nombre; this.apellido = apellido;
    }
    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
}
