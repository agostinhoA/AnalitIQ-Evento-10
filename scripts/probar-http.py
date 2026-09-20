"""Pruebas HTTP del evento 10 corregido. Requiere MySQL demo y scripts 01, 02, 04 y 05.
Uso: python scripts/probar-http.py http://127.0.0.1:8080/analitiq
"""
import concurrent.futures
import datetime as dt
import http.cookiejar
import re
import sys
import unittest
import urllib.error
import urllib.parse
import urllib.request

BASE = sys.argv.pop(1).rstrip('/') if len(sys.argv) > 1 else 'http://127.0.0.1:8080/analitiq'
HOY = dt.datetime.now(dt.timezone(dt.timedelta(hours=-3))).date()

def flujo(url):
    return urllib.parse.parse_qs(urllib.parse.urlparse(url).query)['busqueda'][0]

def cuotas(body):
    return re.findall(r'data-cuota="(\d+)"', body)

def bloques(body):
    return dict(re.findall(r'<article[^>]*data-tratamiento="(\d+)"[^>]*>(.*?)</article>', body, re.S))

class Browser:
    def __init__(self):
        self.jar = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(self.jar))
        status, body, _, _ = self.get('/deudas')
        if status != 200:
            raise RuntimeError('La página inicial no está disponible.')
        self.csrf = re.search(r'name="csrf" value="([^"]+)"', body).group(1)

    def request(self, path, fields=None):
        url = path if path.startswith(('http://', 'https://')) else BASE + path
        data = urllib.parse.urlencode(fields).encode() if fields is not None else None
        try:
            response = self.opener.open(url, data=data, timeout=20)
        except urllib.error.HTTPError as e:
            response = e
        with response:
            return response.status, response.read().decode('utf-8'), response.url, response.headers

    def get(self, path):
        return self.request(path)

    def post(self, **fields):
        return self.request('/deudas', {'csrf': self.csrf, **fields})

    def search(self, dni='', nombre='', apellido='', desde='2026-09-01', hasta='2026-09-30', **extras):
        return self.post(accion='buscar', modo='dni' if dni else 'nombre', dni=dni,
                         nombre=nombre, apellido=apellido, desde=desde, hasta=hasta, **extras)

class FlujosHttp(unittest.TestCase):
    def setUp(self):
        self.b = Browser()

    def test_dni_unico_excluye_consultas_y_pagadas(self):
        status, body, _, headers = self.b.search(dni='30111222', tipo='CONSULTAS', estado='Pagada')
        self.assertEqual(200, status)
        self.assertIn('Deudas de tratamientos activos', body)
        self.assertEqual(['603'], cuotas(body))
        for text in ['Consulta #301', 'Pago #501', 'Pago #503', 'data-cuota="601"', 'data-cuota="602"', 'name="tipo"', 'name="estado"']:
            self.assertNotIn(text, body)
        self.assertIn('30.000,00', body)
        self.assertNotIn('Ã', body)
        self.assertEqual('no-store', headers['Cache-Control'])

    def test_nombre_unico_sin_tratamientos_activos(self):
        status, body, _, _ = self.b.search(nombre='Lucía', apellido='Gómez')
        self.assertEqual(200, status)
        self.assertIn('Este paciente no tiene tratamientos activos.', body)
        self.assertEqual({}, bloques(body))
        self.assertNotIn('Seleccioná al paciente', body)

    def test_sin_coincidencias_conserva_fechas(self):
        for fields in [{'dni': '99999999'}, {'nombre': 'Nadie', 'apellido': 'Inexistente'}]:
            status, body, _, _ = self.b.search(**fields)
            self.assertEqual(200, status)
            self.assertIn('No se encontró un paciente', body)
            self.assertIn('value="2026-09-01"', body)
            self.assertIn('value="2026-09-30"', body)

    def test_homonimos_conservan_rango_aunque_se_manipule(self):
        status, body, url, _ = self.b.search(nombre='Juan', apellido='Pérez', desde='2026-09-01', hasta='2026-09-01')
        self.assertEqual(200, status)
        for text in ['Seleccioná al paciente', '30111222', '30999888', '01/09/2026']:
            self.assertIn(text, body)
        status, body, _, _ = self.b.post(accion='seleccionar', busqueda=flujo(url), dni='30111222',
                                        desde='1900-01-01', hasta='1900-01-01', tipo='CONSULTAS')
        self.assertEqual(200, status)
        self.assertEqual(['603'], cuotas(body))
        self.assertIn('value="2026-09-01"', body)
        self.assertNotIn('value="1900-01-01"', body)

    def test_dni_manipulado_no_accede_a_otro_paciente(self):
        _, _, url, _ = self.b.search(nombre='Juan', apellido='Pérez')
        status, body, _, _ = self.b.post(accion='seleccionar', busqueda=flujo(url), dni='45000001')
        self.assertEqual(400, status)
        self.assertIn('Seleccioná un paciente de los resultados', body)
        self.assertNotIn('data-tratamiento="2101"', body)
        self.assertEqual(200, self.b.post(accion='seleccionar', busqueda=flujo(url), dni='30999888')[0])

    def test_agrupa_tres_activos_con_bloques_vacios(self):
        status, body, _, _ = self.b.search(dni='45000001', hasta='2026-09-15')
        self.assertEqual(200, status)
        grupos = bloques(body)
        self.assertEqual({'2101', '2102', '2103'}, set(grupos))
        self.assertEqual(['2302', '2303'], cuotas(grupos['2101']))
        for codigo in ['2102', '2103']:
            self.assertIn('Este tratamiento activo no tiene deudas en el rango seleccionado.', grupos[codigo])
            self.assertEqual([], cuotas(grupos[codigo]))
        self.assertNotIn('data-cuota="2306"', body)
        self.assertNotIn('data-tratamiento="2104"', body)

    def test_rango_inclusivo_y_un_solo_dia(self):
        _, body, _, _ = self.b.search(dni='45000001', desde='2026-09-01', hasta='2026-09-15')
        self.assertEqual(['2302', '2303'], cuotas(body))
        _, body, _, _ = self.b.search(dni='45000001', desde='2026-09-15', hasta='2026-09-15')
        self.assertEqual(['2303'], cuotas(body))

    def test_fechas_futuras_no_son_deudas(self):
        fechas = {'2301':'2026-08-31','2302':'2026-09-01','2303':'2026-09-15',
                  '2304':'2026-09-30','2305':'2026-10-01','2308':'2026-12-01'}
        _, body, _, _ = self.b.search(dni='45000001', desde='2026-01-01', hasta='2027-12-31')
        expected = [id_ for id_, fecha in fechas.items() if dt.date.fromisoformat(fecha) <= HOY]
        self.assertEqual(expected, cuotas(body))
        tomorrow = (HOY + dt.timedelta(days=1)).isoformat()
        _, body, _, _ = self.b.search(dni='45000001', desde=tomorrow, hasta=tomorrow)
        self.assertEqual([], cuotas(body))
        self.assertEqual(3, len(bloques(body)))

    def test_aplicar_filtro_conserva_paciente_y_otra_pestana(self):
        _, _, anterior, _ = self.b.search(dni='45000001', hasta='2026-09-15')
        status, body, nuevo, _ = self.b.post(accion='filtrar', busqueda=flujo(anterior),
                                            desde='2026-08-31', hasta='2026-08-31', dni='30111222')
        self.assertEqual(200, status)
        self.assertNotEqual(anterior, nuevo)
        self.assertIn('<strong>45000001</strong>', body)
        self.assertEqual(['2301'], cuotas(body))
        self.assertEqual(['2302', '2303'], cuotas(self.b.get(anterior)[1]))

    def test_rechaza_filtro_invertido_sin_cambiar_informe(self):
        _, _, url, _ = self.b.search(dni='45000001', hasta='2026-09-15')
        status, body, _, _ = self.b.post(accion='filtrar', busqueda=flujo(url), desde='2026-09-30', hasta='2026-09-01')
        self.assertEqual(400, status)
        self.assertIn('No se aplicó el filtro', body)
        self.assertEqual(['2302', '2303'], cuotas(body))
        self.assertEqual(['2302', '2303'], cuotas(self.b.get(url)[1]))

    def test_rango_obligatorio_formato_y_validacion_servidor(self):
        for desde, hasta in [('', '2026-09-18'), ('2026-09-18', ''), ('2026-09-20', '2026-09-18'),
                             ('2026-02-30','2026-09-18'), ("2026-09-01' OR 1=1 --",'2026-09-18')]:
            self.assertEqual(400, self.b.search(dni='30111222', desde=desde, hasta=hasta)[0])
        self.assertEqual(400, self.b.post(accion='buscar', modo='dni', dni='30111222')[0])

    def test_inconsistencias_no_ocultan_registros(self):
        for dni, cantidad in [('45000003',2),('45000004',4)]:
            status, body, _, _ = self.b.search(dni=dni)
            self.assertEqual(200, status)
            self.assertEqual(cantidad,len(bloques(body)))
            self.assertIn('Inconsistencia:', body)
        self.assertIn('máximo permitido es 3', body)

    def test_sin_activos_es_distinto_de_sin_deuda(self):
        for dni in ['30999888','35666777','45000002']:
            _, body, _, _ = self.b.search(dni=dni)
            self.assertIn('Este paciente no tiene tratamientos activos.', body)
        for dni in ['37888999','40123456']:
            _, body, _, _ = self.b.search(dni=dni)
            self.assertEqual(1,len(bloques(body)))
            self.assertIn('Este tratamiento activo no tiene deudas en el rango seleccionado.', body)

    def test_sesiones_y_solicitudes_ajenas(self):
        _, _, url, _ = self.b.search(dni='30111222')
        otra = Browser()
        self.assertEqual(400, otra.get(url)[0])
        self.assertEqual(400, otra.post(accion='filtrar', busqueda=flujo(url), desde='2026-09-01', hasta='2026-09-18')[0])
        self.assertEqual(400, self.b.get('/deudas?busqueda=inventado')[0])
        self.assertEqual(403, self.b.post(accion='buscar', csrf='invalido')[0])

    def test_validacion_y_escape(self):
        for fields in [{'dni': "' OR 1=1 --"}, {'nombre': '<script>alert(1)</script>', 'apellido':'Pérez'},
                       {'nombre':'Juan','apellido':''}]:
            status, body, _, _ = self.b.search(**fields)
            self.assertEqual(400, status)
            self.assertNotIn('<script>alert(1)</script>', body)

    def test_concurrencia(self):
        def run(dni):
            status, body, _, _ = Browser().search(dni=dni)
            return status == 200 and f'<strong>{dni}</strong>' in body
        with concurrent.futures.ThreadPoolExecutor(max_workers=8) as executor:
            self.assertTrue(all(executor.map(run, ['30111222','45000001','30999888','45000002'] * 3)))

    def test_inicio_compatibilidad_y_jsp_privadas(self):
        for path in ['/', '/pagos', '/deudas']:
            status, body, _, _ = self.b.get(path)
            self.assertEqual(200,status)
            self.assertIn('Consulta de deudas',body)
            self.assertNotIn('name="tipo"',body)
            self.assertNotIn('name="estado"',body)
        self.assertEqual(404,self.b.get('/WEB-INF/views/informe.jsp')[0])

if __name__ == '__main__':
    unittest.main(verbosity=2)

