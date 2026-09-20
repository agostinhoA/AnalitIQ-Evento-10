# Fuente, alcance y decisiones

> Documento histórico de la primera entrega. La consulta de pagos, el selector de tipo y la inclusión de tratamientos inactivos descritos aquí quedaron sustituidos por [el alcance del coloquio](coloquio-2026-09-17.md): sólo deudas de tratamientos activos con rango obligatorio por vencimiento. Se mantienen las decisiones de nombres/esquema, la búsqueda exacta y la ausencia de moneda en cuotas y presupuestos. Este archivo no implica que se haya modificado el PDF externo.

Fuente disponible: `../Fase de Diseño e Integracion - Tutoria 17.9.pdf` (145 páginas). No había una carpeta docs ni scripts SQL al comenzar. Se conserva el PDF original, sin modificaciones. No se encontró un archivo independiente titulado «evento 10 corregido»; se tomó la especificación detallada más reciente dentro del PDF junto con las correcciones expresas del pedido.

Referencias: estructura normalizada pp. 39–43; DER redefinido p. 44; almacenes pp. 53–54; estructuras del diccionario pp. 69–74; dominios pp. 75–81; procesos 2.1.2.1–2.1.2.7 pp. 88–96.

## Contradicciones y límites señalados antes de implementar

- El dominio `tipo_consulta` (p. 80) sólo enumera consultas o tratamientos, y `informe_pagos` usa una alternativa. El coordinador (pp. 89–90) y el pedido admiten **ambos**. Se aplica esa corrección; no se añade una columna persistida para este parámetro de navegación.
- El pseudocódigo espera la selección dentro de un bucle. Se representa con POST de búsqueda, respuesta 303, GET de resultados y un nuevo POST de selección. Cada solicitud termina; ningún hilo espera al usuario.
- `CUOTAS_X_PAGOS` contiene únicamente sus dos claves. No permite conocer el importe imputado de cada pago a cada cuota. Se presenta el importe completo del pago vinculado y se advierte que puede aparecer en más de una cuota. No se suman pagos repetidos, no se calcula saldo ni se reclasifican estados. El proceso de registro ajeno a este alcance menciona cuotas completas (p. 100); la demo cumple esa condición.
- Presupuestos y cuotas no tienen código de moneda. Se muestran sus importes sin inventar un símbolo ni asumir ARS. Los pagos sí muestran `nombre_moneda` y `cotizacion_aplicada`; esta última es un dato almacenado, sin recalcular conversiones.
- La estructura `tratamiento` define `objetivos`, aunque ciertos flujos dicen `objetivo`. Se respeta `objetivos`, como en la estructura normalizada y el diccionario de entidades.
- El DER nombra entidades en singular; los almacenes y los procesos usan plurales. Las tablas físicas conservan los nombres de almacenes, escritos uniformemente en minúsculas, incluidos `periodos_de_pagos`, `metodos_pagos` y `cuotas_x_pagos`. Esto es necesario para la portabilidad: MySQL en Windows almacena los nombres en minúsculas (`lower_case_table_names=1`) y un dump conservará ese caso al migrar a Linux. DDL, consultas y respaldos deben coincidir; no se renombra ningún concepto del modelo.

## Conversión física explícita

El documento es lógico, no entrega DDL ni longitudes SQL. Se eligieron `VARCHAR(8)` para DNI (7–8 dígitos), `VARCHAR(100)` para nombres, `VARCHAR(10)` para teléfono, `VARCHAR(255)`/`TEXT` para texto, `DATE`/`TIME`, claves `INT UNSIGNED`, `DECIMAL(15,2)` para el dominio racional de dos decimales y `VARCHAR(2)` para SI/NO. Son límites técnicos documentados, no nuevas reglas financieras. Los paréntesis del diccionario se traducen a NULL. Los dominios de estados, métodos, monedas, tratamientos y períodos se conservan.

La búsqueda usa igualdad parametrizada, conforme al proceso 2.1.2.2. Se quitan sólo espacios en los extremos. La colación `utf8mb4_0900_as_cs` distingue mayúsculas y tildes; la interfaz lo informa. Buscar sin tildes, parcialmente o ignorando mayúsculas requeriría acordar otro criterio, no está implícito en este evento.

El subconjunto tiene 13 tablas: PACIENTES, TURNOS, CONSULTAS, TRATAMIENTOS, TIPOS_TRATAMIENTOS, PRESUPUESTOS, PERIODOS_DE_PAGOS, CUOTAS, ESTADOS_CUOTAS, CUOTAS_X_PAGOS, PAGOS, METODOS_PAGOS y TIPOS_MONEDAS. No requiere crear historias clínicas ni consentimientos: sus claves apuntan hacia este subconjunto, no al revés.

Los pagos de consultas se recorren por PACIENTES → TURNOS → CONSULTAS → PAGOS; sólo consultas con pago vinculado. Los tratamientos se consultan sin filtrar `activo`, ya que el evento incluye la información histórica. Sus presupuestos/cuotas se muestran incluso cuando no tienen pagos. Se conservan las posibilidades de varios presupuestos por tratamiento y varios pagos por cuota descritas por el evento.

## Sesiones y acceso

El mapa de flujos vive en `HttpSession`, con identificadores aleatorios, máximo 10 búsquedas y vencimiento a los 15 minutos. Guarda resultados obtenidos de MySQL, tipo y DNI validado; no es un repositorio de datos ficticios. Los importes siempre se leen de MySQL. La selección se verifica contra los resultados de ese flujo; no acepta un DNI arbitrario ni un cambio de tipo enviado por el cliente. Confirmada una selección, no se cambia desde otra solicitud concurrente.

La aplicación no implementa identidad de odontólogos porque el alcance y el modelo suministrado no definen cuentas/permisos. Es una demostración pública únicamente con datos ficticios. Antes de usar datos reales se necesita definir e implementar autenticación y autorización. No se inventó una tabla de usuarios.

Concurrencia: pool de hasta 10 conexiones, una conexión por operación, try-with-resources y transacción de lectura con snapshot consistente para cada informe. DAOs y servicio no tienen estado por paciente. El mapa de sesión sincroniza únicamente operaciones breves; no hay bloqueo mientras se consulta MySQL. Para varias instancias de Tomcat se necesita afinidad de sesión o un mecanismo de sesiones compartidas.
