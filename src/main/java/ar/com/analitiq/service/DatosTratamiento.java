package ar.com.analitiq.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Datos validados antes de acceder a MySQL. TEXT admite 65535 bytes, no caracteres. */
public record DatosTratamiento(String dni,long tipo,LocalDate inicio,LocalDate fin,String objetivos,String pronostico) {
    public DatosTratamiento {
        dni=limpiar(dni);
        if(!dni.matches("[0-9]{7,8}")) throw new IllegalArgumentException("Ingresá un DNI de 7 u 8 dígitos, sin puntos.");
        if(tipo<1 || tipo>4294967295L) throw new IllegalArgumentException("Seleccioná un tipo de tratamiento válido.");
        if(inicio==null || inicio.getYear()<1000 || inicio.getYear()>9999)
            throw new IllegalArgumentException("Ingresá una fecha de inicio válida entre los años 1000 y 9999.");
        if(fin!=null && (fin.getYear()<1000 || fin.getYear()>9999 || fin.isBefore(inicio)))
            throw new IllegalArgumentException("La fecha estimada debe ser válida y no puede ser anterior al inicio.");
        objetivos=texto(objetivos,"Objetivos"); pronostico=texto(pronostico,"Pronóstico");
    }
    public static DatosTratamiento parse(String dni,String tipo,String inicio,String fin,String objetivos,String pronostico) {
        long codigo;
        try { codigo=Long.parseLong(limpiar(tipo)); }
        catch(NumberFormatException e) { throw new IllegalArgumentException("Seleccioná un tipo de tratamiento válido."); }
        return new DatosTratamiento(dni,codigo,fecha(inicio,false),fecha(fin,true),objetivos,pronostico);
    }
    private static LocalDate fecha(String valor,boolean opcional) {
        valor=limpiar(valor);
        if(opcional && valor.isEmpty()) return null;
        try {
            if(!valor.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw new DateTimeParseException("fecha",valor,0);
            return LocalDate.parse(valor);
        } catch(DateTimeParseException e) { throw new IllegalArgumentException("Ingresá fechas válidas; la fecha de inicio es obligatoria."); }
    }
    private static String limpiar(String valor) { return valor==null ? "" : valor.strip(); }
    private static String texto(String valor,String campo) {
        valor=limpiar(valor);
        if(valor.getBytes(StandardCharsets.UTF_8).length>65535)
            throw new IllegalArgumentException("El texto de "+campo+" es demasiado largo. Acortalo para continuar.");
        return valor.isEmpty() ? null : valor;
    }
}
