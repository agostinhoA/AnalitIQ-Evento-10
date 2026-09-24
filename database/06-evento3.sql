-- MySQL 8.4+. Migración aditiva, ejecutar UNA VEZ como administrador, SIN --force.
-- Respaldar antes. Si hay activos duplicados, el ALTER falla sin eliminar datos.
USE analitiq_demo;
-- Ejecutar en una conexión administrativa exclusiva, sin otras escrituras.
-- MySQL 1833 impide añadir AUTO_INCREMENT a una clave referenciada.
-- Sólo en esta conexión se suspende la comprobación mientras se cambia el atributo;
-- no cambian el tipo, valores, claves foráneas ni referencias existentes.
SET FOREIGN_KEY_CHECKS=0;
ALTER TABLE tratamientos
 MODIFY codigo_tratamiento INT UNSIGNED NOT NULL AUTO_INCREMENT,
 ADD COLUMN token_registro CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NULL INVISIBLE,
 ADD COLUMN tipo_activo INT UNSIGNED GENERATED ALWAYS AS
   (CASE WHEN activo = 'SI' THEN codigo_tipo_tratamiento ELSE NULL END) VIRTUAL INVISIBLE,
 ADD UNIQUE KEY uq_tratamiento_solicitud (token_registro),
 ADD UNIQUE KEY uq_tratamiento_activo (dni_paciente,tipo_activo);
SET FOREIGN_KEY_CHECKS=1;
-- Columnas técnicas invisibles: no cambian los INSERT históricos de ocho valores.
-- AUTO_INCREMENT conserva los códigos existentes y sus referencias.
