-- Complemento de 09: Lucía Prado tendrá dos tratamientos con tres cuotas
-- impagas cada uno: dos con vencimiento hasta 11/11/2026 y una en diciembre.
-- Ejecutar UNA VEZ después de 09, sin --force, deteniendo ante errores.
-- Sólo para estos presupuestos ficticios; conserva las cuotas existentes.
USE analitiq_demo;
START TRANSACTION;
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63108,62101,10000.00,1,'2026-12-11',3),
 (63109,62102,15000.00,1,'2026-12-12',3);
UPDATE presupuestos SET monto_total=30000.00,cantidad_cuotas=3 WHERE codigo_presupuesto=62101;
UPDATE presupuestos SET monto_total=45000.00,cantidad_cuotas=3 WHERE codigo_presupuesto=62102;
COMMIT;
