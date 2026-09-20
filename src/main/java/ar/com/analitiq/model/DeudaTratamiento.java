package ar.com.analitiq.model;

import java.util.List;

public final class DeudaTratamiento {
    private final TratamientoActivo tratamiento;
    private final List<Presupuesto> presupuestos;
    public DeudaTratamiento(TratamientoActivo tratamiento, List<Presupuesto> presupuestos) {
        this.tratamiento = tratamiento; this.presupuestos = List.copyOf(presupuestos);
    }
    public TratamientoActivo getTratamiento() { return tratamiento; }
    public List<Presupuesto> getPresupuestos() { return presupuestos; }
}
