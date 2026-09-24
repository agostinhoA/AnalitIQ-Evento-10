-- Verificado con MySQL 8.0.41. Ejecutar SIN --force. Falla si la base ya existe; nunca la reemplaza.
CREATE DATABASE analitiq_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs;
USE analitiq_demo;
CREATE TABLE pacientes (
 dni_paciente VARCHAR(8) PRIMARY KEY, nombre_paciente VARCHAR(100) NOT NULL,
 apellido_paciente VARCHAR(100) NOT NULL, fecha_nacimiento DATE NOT NULL,
 nacionalidad VARCHAR(255), numero_telefono VARCHAR(10) NOT NULL, domicilio TEXT,
 estado_civil VARCHAR(255), email VARCHAR(255) NOT NULL,
 CHECK (dni_paciente REGEXP '^[0-9]{7,8}$'),
 INDEX ix_paciente_nombre (nombre_paciente, apellido_paciente)
) ENGINE=InnoDB;
CREATE TABLE tipos_tratamientos (
 codigo_tipo_tratamiento INT UNSIGNED PRIMARY KEY, nombre_tratamiento VARCHAR(30) NOT NULL,
 descripcion_general TEXT,
 CHECK (nombre_tratamiento IN ('Implante','Ortodoncia','Conducto'))
) ENGINE=InnoDB;
CREATE TABLE tratamientos (
 codigo_tratamiento INT UNSIGNED PRIMARY KEY, codigo_tipo_tratamiento INT UNSIGNED NOT NULL,
 dni_paciente VARCHAR(8) NOT NULL, fecha_inicio_tratamiento DATE NOT NULL,
 fecha_finalizacion_tratamiento_estimada DATE, objetivos TEXT, pronostico TEXT,
 activo VARCHAR(2) NOT NULL CHECK (activo IN ('SI','NO')),
 FOREIGN KEY (codigo_tipo_tratamiento) REFERENCES tipos_tratamientos(codigo_tipo_tratamiento),
 FOREIGN KEY (dni_paciente) REFERENCES pacientes(dni_paciente)
) ENGINE=InnoDB;
CREATE TABLE turnos (
 codigo_turno INT UNSIGNED PRIMARY KEY, dni_paciente VARCHAR(8), fecha DATE NOT NULL, horario TIME NOT NULL,
 FOREIGN KEY (dni_paciente) REFERENCES pacientes(dni_paciente)
) ENGINE=InnoDB;
CREATE TABLE metodos_pagos (
 codigo_metodo_pago INT UNSIGNED PRIMARY KEY, nombre_metodo VARCHAR(30) NOT NULL,
 CHECK (nombre_metodo IN ('efectivo','transferencia'))
) ENGINE=InnoDB;
CREATE TABLE tipos_monedas (
 codigo_tipo_moneda INT UNSIGNED PRIMARY KEY, nombre_moneda VARCHAR(30) NOT NULL,
 CHECK (nombre_moneda IN ('Peso Argentino','Yen','Dólar','Euro'))
) ENGINE=InnoDB;
CREATE TABLE pagos (
 codigo_pago INT UNSIGNED PRIMARY KEY, codigo_metodo_pago INT UNSIGNED NOT NULL,
 monto_pagado DECIMAL(15,2) NOT NULL CHECK (monto_pagado >= 0), fecha_de_pago DATE NOT NULL,
 codigo_tipo_moneda INT UNSIGNED NOT NULL, cotizacion_aplicada DECIMAL(15,2) NOT NULL CHECK (cotizacion_aplicada >= 0),
 FOREIGN KEY (codigo_metodo_pago) REFERENCES metodos_pagos(codigo_metodo_pago),
 FOREIGN KEY (codigo_tipo_moneda) REFERENCES tipos_monedas(codigo_tipo_moneda)
) ENGINE=InnoDB;
CREATE TABLE consultas (
 codigo_consulta INT UNSIGNED PRIMARY KEY, codigo_turno INT UNSIGNED NOT NULL,
 codigo_tratamiento INT UNSIGNED, codigo_pago INT UNSIGNED,
 FOREIGN KEY (codigo_turno) REFERENCES turnos(codigo_turno),
 FOREIGN KEY (codigo_tratamiento) REFERENCES tratamientos(codigo_tratamiento),
 FOREIGN KEY (codigo_pago) REFERENCES pagos(codigo_pago)
) ENGINE=InnoDB;
CREATE TABLE periodos_de_pagos (
 codigo_periodo_pago INT UNSIGNED PRIMARY KEY, nombre_periodo_pago VARCHAR(30) NOT NULL,
 CHECK (nombre_periodo_pago IN ('Mensual','Bimestral','Trimestral','Cuatrimestral'))
) ENGINE=InnoDB;
CREATE TABLE presupuestos (
 codigo_presupuesto INT UNSIGNED PRIMARY KEY, codigo_tratamiento INT UNSIGNED NOT NULL,
 codigo_periodo_pago INT UNSIGNED NOT NULL, monto_total DECIMAL(15,2) NOT NULL CHECK (monto_total >= 0),
 cantidad_cuotas INT UNSIGNED NOT NULL CHECK (cantidad_cuotas > 0),
 FOREIGN KEY (codigo_tratamiento) REFERENCES tratamientos(codigo_tratamiento),
 FOREIGN KEY (codigo_periodo_pago) REFERENCES periodos_de_pagos(codigo_periodo_pago)
) ENGINE=InnoDB;
CREATE TABLE estados_cuotas (
 codigo_estado_cuota INT UNSIGNED PRIMARY KEY, tipo_estado VARCHAR(20) NOT NULL CHECK (tipo_estado IN ('Adeuda','Pagada'))
) ENGINE=InnoDB;
CREATE TABLE cuotas (
 codigo_cuota INT UNSIGNED PRIMARY KEY, codigo_presupuesto INT UNSIGNED NOT NULL,
 monto_cuota DECIMAL(15,2) NOT NULL CHECK (monto_cuota >= 0), codigo_estado_cuota INT UNSIGNED NOT NULL,
 fecha_vencimiento DATE NOT NULL, nro_cuota INT UNSIGNED NOT NULL CHECK (nro_cuota > 0),
 FOREIGN KEY (codigo_presupuesto) REFERENCES presupuestos(codigo_presupuesto),
 FOREIGN KEY (codigo_estado_cuota) REFERENCES estados_cuotas(codigo_estado_cuota)
) ENGINE=InnoDB;
CREATE TABLE cuotas_x_pagos (
 codigo_cuota INT UNSIGNED NOT NULL, codigo_pago INT UNSIGNED NOT NULL,
 PRIMARY KEY (codigo_cuota,codigo_pago),
 FOREIGN KEY (codigo_cuota) REFERENCES cuotas(codigo_cuota),
 FOREIGN KEY (codigo_pago) REFERENCES pagos(codigo_pago)
) ENGINE=InnoDB;
