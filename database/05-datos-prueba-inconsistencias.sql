-- OPCIONAL: casos deliberadamente inconsistentes, sólo en la base ficticia de pruebas.
-- No modifica pacientes previos. Permite verificar los avisos sin LIMIT ni correcciones automáticas.
-- Ejecutar una sola vez SIN --force; ante error hacer ROLLBACK.
USE analitiq_demo;
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO pacientes VALUES
 ('45000003','Eva','Ramos','1990-01-01',NULL,'1100000023',NULL,NULL,'eva.demo@example.invalid'),
 ('45000004','Nora','Gil','1990-01-01',NULL,'1100000024',NULL,NULL,'nora.demo@example.invalid');
INSERT INTO tratamientos VALUES
 (2501,1,'45000003','2026-01-01',NULL,NULL,NULL,'SI'),
 (2502,1,'45000003','2026-02-01',NULL,NULL,NULL,'SI'),
 (2503,1,'45000004','2026-01-01',NULL,NULL,NULL,'SI'),
 (2504,2,'45000004','2026-01-01',NULL,NULL,NULL,'SI'),
 (2505,3,'45000004','2026-01-01',NULL,NULL,NULL,'SI'),
 (2506,1,'45000004','2026-02-01',NULL,NULL,NULL,'SI');
COMMIT;
