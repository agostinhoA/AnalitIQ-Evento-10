-- Sólo datos ficticios; importar una vez en la base de demostración vacía.
USE analitiq_demo;
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO pacientes VALUES
 ('30111222','Juan','Pérez','1985-03-12','Argentina','1100000001',NULL,NULL,'juan.uno@example.invalid'),
 ('30999888','Juan','Pérez','1988-08-20','Argentina','1100000002',NULL,NULL,'juan.dos@example.invalid'),
 ('32444555','Lucía','Gómez','1990-06-05','Argentina','1100000003',NULL,NULL,'lucia@example.invalid'),
 ('35666777','Martina','Ruiz','1993-01-25',NULL,'1100000004',NULL,NULL,'martina@example.invalid'),
 ('37888999','Diego','Sosa','1995-09-10',NULL,'1100000005',NULL,NULL,'diego@example.invalid'),
 ('40123456','Elena','Torres','1997-04-03',NULL,'1100000006',NULL,NULL,'elena@example.invalid');
INSERT INTO tipos_tratamientos VALUES (1,'Ortodoncia','Alineación y corrección de la mordida.'),(2,'Implante','Reposición de una pieza dental.'),(3,'Conducto','Tratamiento endodóntico.');
INSERT INTO metodos_pagos VALUES (1,'efectivo'),(2,'transferencia');
INSERT INTO tipos_monedas VALUES (1,'Peso Argentino'),(2,'Yen'),(3,'Dólar'),(4,'Euro');
INSERT INTO periodos_de_pagos VALUES (1,'Mensual'),(2,'Bimestral'),(3,'Trimestral'),(4,'Cuatrimestral');
INSERT INTO estados_cuotas VALUES (1,'Adeuda'),(2,'Pagada');
INSERT INTO tratamientos VALUES
 (101,1,'30111222','2026-07-01','2027-07-01',NULL,NULL,'SI'),
 (102,3,'32444555','2026-08-01',NULL,NULL,NULL,'NO'),
 (103,2,'37888999','2026-09-01',NULL,NULL,NULL,'SI'),
 (104,1,'40123456','2026-09-01',NULL,NULL,NULL,'SI');
INSERT INTO turnos VALUES (201,'30111222','2026-09-01','10:00:00'),(202,'32444555','2026-09-02','11:00:00'),(203,'35666777','2026-09-03','12:00:00');
INSERT INTO pagos VALUES
 (501,1,15000.00,'2026-09-01',1,1.00),(502,2,25.00,'2026-09-02',3,1200.00),
 (503,2,60000.00,'2026-07-01',1,1.00),(504,1,20000.00,'2026-08-01',1,1.00);
INSERT INTO consultas VALUES (301,201,NULL,501),(302,202,102,502),(303,203,NULL,NULL);
INSERT INTO presupuestos VALUES (401,101,1,90000.00,3),(402,102,2,20000.00,1),(403,104,1,40000.00,2);
INSERT INTO cuotas VALUES
 (601,401,30000.00,2,'2026-07-01',1),(602,401,30000.00,2,'2026-08-01',2),
 (603,401,30000.00,1,'2026-09-01',3),(604,402,20000.00,2,'2026-08-01',1);
-- El pago 503 cubre dos cuotas. Su importe se presenta como importe del pago, no de la imputación.
INSERT INTO cuotas_x_pagos VALUES (601,503),(602,503),(604,504);
COMMIT;
