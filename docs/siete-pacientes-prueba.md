# Siete pacientes adicionales — 24/09/2026

Se agregó `database/08-siete-pacientes-prueba.sql` y se ejecutó en la demo local, con respaldo previo en `.local`. Sólo inserta: conserva los pacientes y registros anteriores. La carga agrega siete pacientes, siete tratamientos activos, siete presupuestos y 21 cuotas pendientes. No hay activos duplicados por paciente y tipo.

| DNI | Paciente ficticio | Tratamiento activo | Total de tres cuotas | Ejemplo de nuevo tratamiento permitido |
|---|---|---|---|---|
| 46000001 | Ana López | Ortodoncia | 30.000,00 | Implante |
| 46000002 | Ana López | Implante | 45.000,00 | Conducto |
| 46000003 | Bruno Díaz | Conducto | 24.000,00 | Ortodoncia |
| 46000004 | Carla Ríos | Ortodoncia | 36.000,00 | Implante |
| 46000005 | Daniel Vega | Implante | 54.000,00 | Conducto |
| 46000006 | Elisa Paz | Conducto | 27.000,00 | Ortodoncia |
| 46000007 | Franco León | Ortodoncia | 42.000,00 | Implante |

En el informe, usar Desde **01/09/2026**, Hasta **30/11/2026**, **Todos los tratamientos**. Cada paciente tiene cuotas Adeuda el 05/09, 05/10 y 05/11. DNI/nombre/apellido vacíos permiten listar los deudores, incluidos los pacientes anteriores que coincidan. Buscar Ana López permite probar homónimos con distinto DNI.

Para registrar un tratamiento, usar el DNI y un tipo distinto al activo (última columna). Inicio obligatorio; finalización, objetivos y pronóstico pueden quedar vacíos. Un tipo activo repetido debe rechazarse. Después del alta, el nuevo tratamiento aún no tiene cuotas: no cambia el informe de deuda.

Pronóstico ya admitía vacío en navegador y servidor y se persistía como NULL. Se hizo explícita la etiqueta **Pronóstico (opcional)**; lo mismo para Objetivos. No se cambió la regla de negocio.

El script 08 es opcional y de una sola ejecución: requiere la demo original y la migración 06. No volver a importarlo en esta computadora. Ante una colisión o error detenerse y hacer ROLLBACK; no usar `--force` ni reemplazar registros.

Las pruebas con cantidades exactas de pacientes corresponden a los scripts originales 01/02/04/05/06, sin 08. Para repetirlas, apuntar la configuración de pruebas a la base aislada con esos fixtures, no a la demo ampliada de uso manual.
