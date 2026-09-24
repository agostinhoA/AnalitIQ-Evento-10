"""Pruebas HTTP del evento 10 (23/09/2026). MySQL demo con scripts 01/02/04/05.
Uso: python scripts/probar-http.py http://127.0.0.1:8080/analitiq
"""
import concurrent.futures
import html
import http.cookiejar
import re
import sys
import unittest
import urllib.error
import urllib.parse
import urllib.request
from decimal import Decimal

BASE = sys.argv.pop(1).rstrip('/') if len(sys.argv) > 1 else 'http://127.0.0.1:8080/analitiq'
TITULO = 'Ver informe de deudas de un paciente'
VACIO = 'No se encontraron cuotas pendientes para los criterios seleccionados'

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

    def search(self, dni='', nombre='', apellido='', tratamiento='', desde='2026-09-01', hasta='2026-09-30', **extras):
        return self.post(accion='buscar', dni=dni, nombre=nombre, apellido=apellido,
                         tratamiento=tratamiento, desde=desde, hasta=hasta, **extras)

class FlujosHttp(unittest.TestCase):
    def setUp(self):
        self.b = Browser()

    def test_dni_unico_excluye_consultas_y_pagadas(self):
        status, body, _, headers = self.b.search(dni='30111222', tipo='CONSULTAS', estado='Pagada')
        self.assertEqual(200, status)
        self.assertIn(TITULO, body)
        self.assertEqual(['603'], cuotas(body))
        for text in ['Consulta #301', 'Pago #501', 'Pago #503', 'data-cuota="601"', 'data-cuota="602"', 'name="tipo"', 'name="estado"', 'Monto total del presupuesto']:
            self.assertNotIn(text, body)
        self.assertIn('30.000,00', body)
        self.assertIn('data-total="30000.00"', body)
        self.assertNotIn('Ã', body)
        self.assertEqual('no-store', headers['Cache-Control'])

    def test_nombre_sin_cuotas_pendientes_no_se_ofrece(self):
        status, body, _, _ = self.b.search(nombre='Lucía', apellido='Gómez')
        self.assertEqual(200, status)
        self.assertIn('No se encontraron pacientes con cuotas pendientes', body)
        self.assertNotIn('32444555', body)
        self.assertEqual({}, bloques(body))
        self.assertNotIn('report-header', body)

    def test_sin_criterio_lista_completa_y_espera_seleccion(self):
        status, body, url, _ = self.b.search(tratamiento='Implante', desde='2026-01-01', hasta='2027-12-31')
        self.assertEqual(200, status)
        self.assertIn('Pacientes con cuotas pendientes', body)
        self.assertEqual(1, len(re.findall(r'class="patient-option"', body)))
        self.assertNotIn('45000004', body)
        self.assertEqual([], cuotas(body))
        self.assertNotIn('report-header', body)
        status, body, _, _ = self.b.post(accion='seleccionar', busqueda=flujo(url), dni='45000001',
                                       tratamiento='Ortodoncia', desde='1900-01-01', hasta='1900-01-01')
        self.assertEqual(200, status)
        self.assertEqual(['2308'], cuotas(body))
        self.assertEqual({'2102'}, set(bloques(body)))
        self.assertIn('value="Implante"', body)
        self.assertIn('value="2027-12-31"', body)

    def test_encabezado_y_selector_comparten_catalogo(self):
        for path in ['/', '/deudas', '/tratamientos/registrar']:
            status, body, _, _ = self.b.get(path)
            self.assertEqual(200, status)
            nav = re.search(r'<nav aria-label="Principal">(.*?)</nav>', body, re.S).group(1)
            self.assertEqual(1, nav.count('<a '))
            self.assertIn('>Inicio</a>', nav)
        _, body, _, _ = self.b.get('/deudas')
        self.assertIn('<select id="tratamiento" name="tratamiento"', body)
        self.assertNotIn('<input id="tratamiento"', body)
        for nombre in ['Conducto', 'Implante', 'Ortodoncia']:
            self.assertIn('value="'+nombre+'"', body)

    def test_cambiar_paciente_regresa_a_la_lista_filtrada(self):
        status, listado, busqueda, _ = self.b.search()
        self.assertEqual(200, status)
        self.assertEqual(2, listado.count('class="patient-option"'))
        self.assertNotIn('45000004', listado)  # Nora Gil no tiene cuotas.
        _, informe, informe_url, _ = self.b.post(accion='seleccionar', busqueda=flujo(busqueda), dni='45000001')
        self.assertIn('<strong>45000001</strong>', informe)
        self.assertNotEqual(busqueda, informe_url)
        enlace = html.unescape(re.search(r'href="([^"]+)">Cambiar paciente</a>', informe).group(1))
        self.assertIn('vista=seleccionar', enlace)
        status, candidatos, _, _ = self.b.get(urllib.parse.urljoin(BASE+'/', enlace))
        self.assertEqual(200, status)
        self.assertEqual(2, candidatos.count('class="patient-option"'))
        self.assertIn('30111222', candidatos)
        self.assertNotIn('45000004', candidatos)
        status, otro, _, _ = self.b.post(accion='seleccionar', busqueda=flujo(informe_url), dni='30111222')
        self.assertEqual(200, status)
        self.assertIn('<strong>30111222</strong>', otro)
        self.assertIn('<strong>45000001</strong>', self.b.get(informe_url)[1])

    def test_sin_coincidencias_limpia_informe_y_conserva_campos(self):
        self.b.search(dni='30111222')
        for fields in [{'dni': '99999999'}, {'nombre': 'Nadie', 'apellido': 'Inexistente'}]:
            status, body, _, _ = self.b.search(tratamiento='Ortodoncia', **fields)
            self.assertEqual(200, status)
            self.assertIn('No se encontraron pacientes', body)
            self.assertIn('value="2026-09-01"', body)
            self.assertIn('value="2026-09-30"', body)
            self.assertIn('value="Ortodoncia"', body)
            self.assertNotIn('report-header', body)
            self.assertEqual([], cuotas(body))
            for valor in fields.values():
                self.assertIn('value="'+valor+'"', body)

    def test_homonimos_conservan_filtros_aunque_se_manipulen(self):
        status, body, url, _ = self.b.search(nombre='Juan', apellido='Pérez', tratamiento='Ortodoncia',
                                          desde='2026-09-01', hasta='2026-09-01')
        self.assertEqual(200, status)
        for text in ['30111222', '01/09/2026', 'Ortodoncia']:
            self.assertIn(text, body)
        self.assertNotIn('30999888', body)
        self.assertEqual(['603'], cuotas(body))
        status, listado, _, _ = self.b.get('/deudas?busqueda='+flujo(url)+'&vista=seleccionar')
        self.assertEqual(200, status)
        self.assertEqual(1, len(re.findall(r'class="patient-option"', listado)))
        status, body, _, _ = self.b.post(accion='seleccionar', busqueda=flujo(url), dni='30111222',
                                       tratamiento='Implante', desde='1900-01-01', hasta='1900-01-01')
        self.assertEqual(200, status)
        self.assertEqual(['603'], cuotas(body))
        self.assertIn('value="Ortodoncia"', body)
        self.assertIn('value="2026-09-01"', body)
        self.assertNotIn('value="1900-01-01"', body)

    def test_dni_manipulado_no_accede_a_otro_paciente(self):
        _, _, url, _ = self.b.search()
        status, body, _, _ = self.b.post(accion='seleccionar', busqueda=flujo(url), dni='45000004')
        self.assertEqual(400, status)
        self.assertIn('Seleccioná un paciente de los resultados', body)
        self.assertEqual([], cuotas(body))
        self.assertEqual(200, self.b.post(accion='seleccionar', busqueda=flujo(url), dni='45000001')[0])

    def test_solo_bloques_con_cuotas_y_tabla_solicitada(self):
        status, body, _, _ = self.b.search(dni='45000001', hasta='2026-09-15')
        self.assertEqual(200, status)
        grupos = bloques(body)
        self.assertEqual({'2101'}, set(grupos))
        self.assertEqual(['2302', '2303'], cuotas(grupos['2101']))
        self.assertIn('Saldo pendiente', body)
        self.assertNotIn('Estado registrado', body)
        self.assertIn('data-cantidad="2"', body)
        self.assertIn('data-total="20000.00"', body)
        self.assertEqual(1, body.count('class="patient-card report-header"'))

    def test_rango_inclusivo_y_un_solo_dia(self):
        _, body, _, _ = self.b.search(dni='45000001', desde='2026-09-01', hasta='2026-09-15')
        self.assertEqual(['2302', '2303'], cuotas(body))
        _, body, _, _ = self.b.search(dni='45000001', desde='2026-09-15', hasta='2026-09-15')
        self.assertEqual(['2303'], cuotas(body))

    def test_sin_corte_por_hoy_orden_cantidades_totales_y_no_duplicados(self):
        _, body, _, _ = self.b.search(dni='45000001', desde='2026-01-01', hasta='2027-12-31')
        self.assertEqual(['2301', '2302', '2303', '2304', '2305', '2308'], cuotas(body))
        self.assertEqual(6, len(set(cuotas(body))))
        grupos = bloques(body)
        self.assertEqual({'2101','2102'}, set(grupos))
        for id_, grupo in grupos.items():
            rows = re.findall(r'<tr data-cuota=.*?</tr>', grupo, re.S)
            amounts = re.findall(r'class="numeric amount">([\d.,]+)</td>', grupo)
            suma = sum((Decimal(x.replace('.','').replace(',','.')) for x in amounts), Decimal('0'))
            total = Decimal(re.search(r'data-total="([^"]+)"', grupo).group(1))
            cantidad = int(re.search(r'data-cantidad="(\d+)"', grupo).group(1))
            self.assertEqual(len(rows), cantidad)
            self.assertEqual(suma, total)
            self.assertEqual(Decimal('50000.00') if id_=='2101' else Decimal('20000.00'), total)
            fechas = re.findall(r'<td>(\d{2}/\d{2}/\d{4})</td>', grupo)
            orden = ['-'.join(reversed(f.split('/'))) for f in fechas]
            self.assertEqual(sorted(orden), orden)
        _, body, _, _ = self.b.search(dni='45000001', desde='2026-12-01', hasta='2026-12-01')
        self.assertEqual(['2308'], cuotas(body))

    def test_tratamiento_especifico_y_nombre_inexistente(self):
        _, body, _, _ = self.b.search(dni='45000001', tratamiento='Implante', desde='2026-01-01', hasta='2027-12-31')
        self.assertEqual(['2308'], cuotas(body))
        status, body, _, _ = self.b.search(dni='45000001', tratamiento='Conducto')
        self.assertEqual(200, status)
        self.assertIn('No se encontraron pacientes con cuotas pendientes', body)
        for nombre in ['No existe', "%' OR 1=1 --"]:
            status, body, _, _ = self.b.search(dni='45000001', tratamiento=nombre)
            self.assertEqual(400, status)
            self.assertIn('Elegí un tratamiento del catálogo', body)
            self.assertEqual({}, bloques(body))

    def test_aplicar_filtro_conserva_paciente_y_otra_pestana(self):
        _, _, anterior, _ = self.b.search(dni='45000001', hasta='2026-09-15')
        status, body, nuevo, _ = self.b.post(accion='filtrar', busqueda=flujo(anterior), tratamiento='Implante',
                                            desde='2026-01-01', hasta='2027-12-31', dni='30111222')
        self.assertEqual(200, status)
        self.assertNotEqual(anterior, nuevo)
        self.assertIn('<strong>45000001</strong>', body)
        self.assertEqual(['2308'], cuotas(body))
        self.assertEqual(['2302','2303'], cuotas(self.b.get(anterior)[1]))

    def test_rechaza_filtro_invertido_sin_cambiar_informe(self):
        _, _, url, _ = self.b.search(dni='45000001', hasta='2026-09-15')
        status, body, _, _ = self.b.post(accion='filtrar', busqueda=flujo(url), desde='2026-09-30', hasta='2026-09-01')
        self.assertEqual(400, status)
        self.assertIn('No se aplicó el filtro', body)
        self.assertEqual(['2302', '2303'], cuotas(body))
        self.assertEqual(['2302', '2303'], cuotas(self.b.get(url)[1]))

    def test_rango_obligatorio_y_calendario(self):
        for desde, hasta in [('', '2026-09-18'), ('2026-09-18', ''), ('2026-09-20', '2026-09-18'),
                             ('2026-02-30','2026-09-18'), ("2026-09-01' OR 1=1 --",'2026-09-18')]:
            self.assertEqual(400, self.b.search(dni='30111222', desde=desde, hasta=hasta)[0])
        self.assertEqual(400, self.b.post(accion='buscar', dni='30111222')[0])

    def test_historicos_no_generan_inconsistencias_ni_bloques_vacios(self):
        for dni in ['45000003','45000004']:
            status, body, _, _ = self.b.search(dni=dni, tratamiento='Implante')
            self.assertEqual(200, status)
            self.assertEqual({}, bloques(body))
            self.assertNotIn('Inconsistencia:', body)
            self.assertIn('No se encontraron pacientes con cuotas pendientes', body)
            self.assertNotIn('máximo permitido', body)

    def test_tratamiento_ignora_mayusculas_y_conserva_filtro(self):
        for nombre in ['ortodoncia', 'Ortodoncia', 'ORTODONCIA', ' oRtOdOnCiA ']:
            status, body, _, _ = self.b.search(dni='45000001', tratamiento=nombre, hasta='2026-09-15')
            self.assertEqual(200, status)
            self.assertEqual(['2302', '2303'], cuotas(body))
        _, _, url, _ = self.b.search(tratamiento='implante', desde='2026-01-01', hasta='2027-12-31')
        status, body, _, _ = self.b.post(accion='seleccionar', busqueda=flujo(url), dni='45000001')
        self.assertEqual(200, status)
        self.assertEqual(['2308'], cuotas(body))

    def test_sin_deudas_con_identificacion_y_rango(self):
        for dni in ['30999888','35666777','45000002','37888999','40123456']:
            _, body, _, _ = self.b.search(dni=dni)
            self.assertIn('No se encontraron pacientes con cuotas pendientes', body)
            self.assertEqual({}, bloques(body))
            self.assertNotIn('report-header', body)

    def test_sesiones_y_solicitudes_ajenas(self):
        _, _, url, _ = self.b.search(dni='30111222')
        otra = Browser()
        self.assertEqual(400, otra.get(url)[0])
        self.assertEqual(400, otra.post(accion='filtrar', busqueda=flujo(url), desde='2026-09-01', hasta='2026-09-18')[0])
        self.assertEqual(400, self.b.get('/deudas?busqueda=inventado')[0])
        self.assertEqual(403, self.b.post(accion='buscar', csrf='invalido')[0])

    def test_validacion_criterios_contradictorios_incompletos_escape(self):
        for fields in [{'dni': "' OR 1=1 --"}, {'nombre': '<script>alert(1)</script>', 'apellido':'Pérez'},
                       {'nombre':'Juan'}, {'apellido':'Pérez'}, {'dni':'30111222','nombre':'Juan','apellido':'Pérez'},
                       {'dni':'30111222','nombre':'Juan'}, {'tratamiento':'x'*31}]:
            status, body, _, _ = self.b.search(**fields)
            self.assertEqual(400, status)
            self.assertNotIn('<script>alert(1)</script>', body)
            self.assertNotIn('Lista de pacientes', body)

    def test_nueva_busqueda_no_reutiliza_seleccion(self):
        self.b.search(dni='45000001')
        _, body, _, _ = self.b.get('/deudas')
        self.assertNotIn('45000001', body)
        _, body, _, _ = self.b.search()
        self.assertIn('Pacientes con cuotas pendientes', body)
        self.assertEqual([], cuotas(body))
        self.assertNotIn('report-header', body)

    def test_concurrencia(self):
        def run(dni):
            status, body, _, _ = Browser().search(dni=dni)
            return status == 200 and f'<strong>{dni}</strong>' in body
        with concurrent.futures.ThreadPoolExecutor(max_workers=8) as executor:
            self.assertTrue(all(executor.map(run, ['30111222','45000001'] * 6)))

    def test_inicio_compatibilidad_y_jsp_privadas(self):
        status, body, _, _ = self.b.get('/')
        self.assertEqual(200, status)
        self.assertIn('Tu consultorio, en un solo lugar.', body)
        for path in ['/pagos', '/deudas']:
            status, body, _, _ = self.b.get(path)
            self.assertEqual(200,status)
            self.assertIn(TITULO,body)
            self.assertNotIn('name="tipo"',body)
            self.assertNotIn('name="estado"',body)
        self.assertEqual(404,self.b.get('/WEB-INF/views/informe.jsp')[0])

if __name__ == '__main__':
    unittest.main(verbosity=2)
