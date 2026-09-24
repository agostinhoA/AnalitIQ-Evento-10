-- Siete pacientes ficticios adicionales para probar eventos 3 y 10.
-- Requiere 01/02 y migración 06. Ejecutar UNA VEZ en conexión exclusiva.
-- Sólo INSERT; no reemplaza ni borra registros. Ante error, detener y ROLLBACK.
-- No usar --force. DNI 46000001–46000007, presupuestos 62001–62007,
-- cuotas 63001–63021. Los códigos de tratamiento los genera MySQL.
USE analitiq_demo;
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO pacientes (dni_paciente,nombre_paciente,apellido_paciente,fecha_nacimiento,numero_telefono,email)
 VALUES ('46000001','Ana','López','1990-01-01','1100000101','prueba.46000001@example.invalid');
INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo)
 VALUES (1,'46000001','2026-08-24',NULL,NULL,NULL,'SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62001,@analitiq_tratamiento,1,30000.00,3);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63001,62001,10000.00,1,'2026-09-05',1),
 (63002,62001,10000.00,1,'2026-10-05',2),
 (63003,62001,10000.00,1,'2026-11-05',3);
INSERT INTO pacientes (dni_paciente,nombre_paciente,apellido_paciente,fecha_nacimiento,numero_telefono,email)
 VALUES ('46000002','Ana','López','1990-01-02','1100000102','prueba.46000002@example.invalid');
INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo)
 VALUES (2,'46000002','2026-08-24',NULL,NULL,NULL,'SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62002,@analitiq_tratamiento,1,45000.00,3);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63004,62002,15000.00,1,'2026-09-05',1),
 (63005,62002,15000.00,1,'2026-10-05',2),
 (63006,62002,15000.00,1,'2026-11-05',3);
INSERT INTO pacientes (dni_paciente,nombre_paciente,apellido_paciente,fecha_nacimiento,numero_telefono,email)
 VALUES ('46000003','Bruno','Díaz','1990-01-03','1100000103','prueba.46000003@example.invalid');
INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo)
 VALUES (3,'46000003','2026-08-24',NULL,NULL,NULL,'SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62003,@analitiq_tratamiento,1,24000.00,3);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63007,62003,8000.00,1,'2026-09-05',1),
 (63008,62003,8000.00,1,'2026-10-05',2),
 (63009,62003,8000.00,1,'2026-11-05',3);
INSERT INTO pacientes (dni_paciente,nombre_paciente,apellido_paciente,fecha_nacimiento,numero_telefono,email)
 VALUES ('46000004','Carla','Ríos','1990-01-04','1100000104','prueba.46000004@example.invalid');
INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo)
 VALUES (1,'46000004','2026-08-24',NULL,NULL,NULL,'SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62004,@analitiq_tratamiento,1,36000.00,3);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63010,62004,12000.00,1,'2026-09-05',1),
 (63011,62004,12000.00,1,'2026-10-05',2),
 (63012,62004,12000.00,1,'2026-11-05',3);
INSERT INTO pacientes (dni_paciente,nombre_paciente,apellido_paciente,fecha_nacimiento,numero_telefono,email)
 VALUES ('46000005','Daniel','Vega','1990-01-05','1100000105','prueba.46000005@example.invalid');
INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo)
 VALUES (2,'46000005','2026-08-24',NULL,NULL,NULL,'SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62005,@analitiq_tratamiento,1,54000.00,3);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63013,62005,18000.00,1,'2026-09-05',1),
 (63014,62005,18000.00,1,'2026-10-05',2),
 (63015,62005,18000.00,1,'2026-11-05',3);
INSERT INTO pacientes (dni_paciente,nombre_paciente,apellido_paciente,fecha_nacimiento,numero_telefono,email)
 VALUES ('46000006','Elisa','Paz','1990-01-06','1100000106','prueba.46000006@example.invalid');
INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo)
 VALUES (3,'46000006','2026-08-24',NULL,NULL,NULL,'SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62006,@analitiq_tratamiento,1,27000.00,3);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63016,62006,9000.00,1,'2026-09-05',1),
 (63017,62006,9000.00,1,'2026-10-05',2),
 (63018,62006,9000.00,1,'2026-11-05',3);
INSERT INTO pacientes (dni_paciente,nombre_paciente,apellido_paciente,fecha_nacimiento,numero_telefono,email)
 VALUES ('46000007','Franco','León','1990-01-07','1100000107','prueba.46000007@example.invalid');
INSERT INTO tratamientos (codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,fecha_finalizacion_tratamiento_estimada,objetivos,pronostico,activo)
 VALUES (1,'46000007','2026-08-24',NULL,NULL,NULL,'SI');
SET @analitiq_tratamiento = LAST_INSERT_ID();
INSERT INTO presupuestos (codigo_presupuesto,codigo_tratamiento,codigo_periodo_pago,monto_total,cantidad_cuotas)
 VALUES (62007,@analitiq_tratamiento,1,42000.00,3);
INSERT INTO cuotas (codigo_cuota,codigo_presupuesto,monto_cuota,codigo_estado_cuota,fecha_vencimiento,nro_cuota) VALUES
 (63019,62007,14000.00,1,'2026-09-05',1),
 (63020,62007,14000.00,1,'2026-10-05',2),
 (63021,62007,14000.00,1,'2026-11-05',3);
COMMIT;
