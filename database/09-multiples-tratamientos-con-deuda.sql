-- Datos ficticios adicionales: dos pacientes con deuda en distintos tipos activos.
-- Requiere estructura 01, catálogos 02 y migración 06. Ejecutar UNA VEZ.
-- No borra ni reemplaza datos. Ante error, detener sin --force: la desconexión
-- revierte la transacción. Los DNI y códigos reservados deben estar libres.
USE analitiq_demo;
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO pacientes (dni_paciente,nombre_paciente,apellido_paciente,fecha_nacimiento,numero_telefono,email) VALUES
 ('47000001','Lucía','Prado','1992-04-12','1100000201','prueba.47000001@example.invalid'),
 ('47000002','Tomás','Vidal','1988-06-15','1100000202','prueba.47000002@example.invalid');

INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,activo)
 VALUES (1,'47000001','2026-10-01','SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62101,@analitiq_tratamiento,1,20000.00,2);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63101,62101,10000.00,1,'2026-11-01',1),
 (63102,62101,10000.00,1,'2026-11-11',2);

INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,activo)
 VALUES (2,'47000001','2026-10-01','SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62102,@analitiq_tratamiento,1,30000.00,2);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63103,62102,15000.00,1,'2026-11-05',1),
 (63104,62102,15000.00,1,'2026-11-11',2);

-- Tercer tipo activo, sin deuda: debe quedar fuera del informe y del total.
INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,activo)
 VALUES (3,'47000001','2026-10-01','SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62103,@analitiq_tratamiento,1,9000.00,1);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota)
 VALUES (63105,62103,9000.00,2,'2026-11-01',1);

INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,activo)
 VALUES (1,'47000002','2026-10-01','SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62104,@analitiq_tratamiento,1,12000.00,1);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota)
 VALUES (63106,62104,12000.00,1,'2026-11-01',1);

INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,activo)
 VALUES (3,'47000002','2026-10-01','SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62105,@analitiq_tratamiento,1,8000.00,1);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota)
 VALUES (63107,62105,8000.00,1,'2026-11-05',1);
COMMIT;
