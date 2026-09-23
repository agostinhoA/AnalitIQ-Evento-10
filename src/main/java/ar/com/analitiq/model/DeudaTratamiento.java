package ar.com.analitiq.model;

import java.math.BigDecimal;
import java.util.List;

public final class DeudaTratamiento {
    private final TratamientoActivo tratamiento;
    private final List<Cuota> cuotas;
    private final BigDecimal total;
    public DeudaTratamiento(TratamientoActivo tratamiento, List<Cuota> cuotas) {
        this.tratamiento=tratamiento; this.cuotas=List.copyOf(cuotas);
        this.total=this.cuotas.stream().map(Cuota::getMonto).reduce(BigDecimal.ZERO,BigDecimal::add);
    }
    public TratamientoActivo getTratamiento() { return tratamiento; }
    public List<Cuota> getCuotas() { return cuotas; }
    public int getCantidad() { return cuotas.size(); }
    public BigDecimal getTotal() { return total; }
}
