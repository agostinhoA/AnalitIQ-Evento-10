"""Pruebas destructivas sólo sobre fixtures propios en analitiq_evento3_test.
Usar Tomcat separado (8081), conectado a ese esquema, nunca a una base clínica.
Variables: ANALITIQ_TEST_MYSQL_EXE, ANALITIQ_TEST_ADMIN_USER; contraseña opcional
ANALITIQ_TEST_ADMIN_PASSWORD. No se imprimen secretos. Ver docs/evento3.md.
"""
import concurrent.futures
import html
import http.cookiejar
import os
import re
import subprocess
import sys
import unittest
import urllib.error
import urllib.parse
import urllib.request

BASE = sys.argv.pop(1).rstrip('/') if len(sys.argv)>1 else 'http://127.0.0.1:8081/analitiq'
if urllib.parse.urlparse(BASE).hostname not in ('127.0.0.1','localhost'):
    raise RuntimeError('Esta suite sólo permite el Tomcat local de pruebas.')
PATH = '/tratamientos/registrar'

def sql(query):
    env=os.environ.copy()
    env['MYSQL_PWD']=os.environ.get('ANALITIQ_TEST_ADMIN_PASSWORD','')
    args=[os.environ['ANALITIQ_TEST_MYSQL_EXE'],'--host=127.0.0.1',
          '--user='+os.environ['ANALITIQ_TEST_ADMIN_USER'],'--default-character-set=utf8mb4',
          '--batch','--skip-column-names','analitiq_evento3_test']
    p=subprocess.run(args,input=query,encoding='utf-8',capture_output=True,env=env)
    if p.returncode: raise RuntimeError('Error en la preparación o verificación del esquema aislado.')
    return p.stdout.strip()

def hidden(body,name):
    return html.unescape(re.search(r'name="'+name+r'" value="([^"]*)"',body).group(1))

class Browser:
    def __init__(self):
        self.opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
        status,body,_=self.get(PATH)
        if status!=200: raise RuntimeError('No está disponible el formulario en el Tomcat de pruebas.')
        self.csrf=hidden(body,'csrf'); self.token=hidden(body,'token')
    def request(self,path,fields=None):
        url=path if path.startswith('http') else BASE+path
        data=urllib.parse.urlencode(fields).encode() if fields is not None else None
        try: response=self.opener.open(url,data=data,timeout=20)
        except urllib.error.HTTPError as e: response=e
        with response: return response.status,response.read().decode('utf-8'),response.url
    def get(self,path): return self.request(path)
    def post(self,**values):
        fields=dict(csrf=self.csrf,token=self.token,dni='88100001',tipo='1',inicio='2026-09-23',fin='',objetivos='',pronostico='')
        fields.update(values); return self.request(PATH,fields)

class RegistroHttp(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if sql('SELECT DATABASE();')!='analitiq_evento3_test': raise RuntimeError('Esquema incorrecto')
        cls.original=sql('SELECT COUNT(*) FROM presupuestos; SELECT COUNT(*) FROM cuotas; SELECT COUNT(*) FROM pagos;')
        cls.cleanup()
        for i in range(1,10):
            sql(f"INSERT INTO pacientes VALUES('8810000{i}','Prueba','HTTP','2000-01-01',NULL,'1100000000',NULL,NULL,'http@example.invalid');")
        # Sólo pacientes de esta suite. Uno con histórico y otro con activo.
        sql("INSERT INTO tratamientos(codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,activo) VALUES(1,'88100003','2025-01-01','NO'),(1,'88100004','2026-01-01','SI');")
    @classmethod
    def cleanup(cls):
        sql("DELETE FROM tratamientos WHERE dni_paciente BETWEEN '88100001' AND '88100009'; DELETE FROM pacientes WHERE dni_paciente BETWEEN '88100001' AND '88100009';")
    @classmethod
    def tearDownClass(cls):
        after=sql('SELECT COUNT(*) FROM presupuestos; SELECT COUNT(*) FROM cuotas; SELECT COUNT(*) FROM pagos;')
        cls.cleanup()
        if after!=cls.original: raise AssertionError('Se modificaron tablas ajenas al registro.')
    def setUp(self): self.b=Browser()
    def test_menu_y_catalogo(self):
        status,body,_=self.b.get('/')
        self.assertEqual(200,status)
        self.assertIn('Tu consultorio, en un solo lugar.',body)
        for link in ['/tratamientos/registrar','/deudas']: self.assertIn(link,body)
        status,body,_=self.b.get(PATH)
        for name in ['Ortodoncia','Implante','Conducto','Objetivos']: self.assertIn(name,body)
        self.assertNotIn('Sólo lectura',body)
    def test_alta_obligatoria_y_reenvio(self):
        status,body,url=self.b.post()
        self.assertEqual(200,status); self.assertIn('Tratamiento registrado',body)
        code=re.search(r'data-registro="(\d+)"',body).group(1)
        status,again,_=self.b.post()
        self.assertEqual(200,status); self.assertIn(f'data-registro="{code}"',again)
        self.assertEqual('1',sql("SELECT COUNT(*) FROM tratamientos WHERE dni_paciente='88100001';"))
        self.assertEqual('1',sql("SELECT COUNT(*) FROM tratamientos WHERE dni_paciente='88100001' AND objetivos IS NULL AND pronostico IS NULL AND fecha_finalizacion_tratamiento_estimada IS NULL;"))
        self.assertEqual(200,self.b.get(url)[0])
    def test_opcionales_y_escape(self):
        status,body,_=self.b.post(dni='88100002',tipo='2',fin='2027-01-01',objetivos='Recuperar función 🙂',pronostico='<script>alert(1)</script>')
        self.assertEqual(200,status); self.assertNotIn('<script>',body)
        self.assertEqual('Recuperar función 🙂',sql("SELECT objetivos FROM tratamientos WHERE dni_paciente='88100002';"))
    def test_fechas_y_datos_preservados(self):
        for values in [dict(inicio=''),dict(inicio='2026-02-30'),dict(fin='2026-09-22'),dict(dni='30.111.222')]:
            fields=dict(dni='88100006',objetivos='<b>Conservar</b>')
            fields.update(values)
            status,body,_=self.b.post(**fields)
            self.assertEqual(400,status)
            if 'dni' not in values: self.assertIn('&lt;b&gt;Conservar&lt;/b&gt;',body)
            self.assertIn(f'value="{self.b.token}"',body)
        self.assertEqual(200,self.b.post(dni='88100006')[0])
    def test_paciente_inexistente(self):
        status,body,_=self.b.post(dni='99999999')
        self.assertEqual(400,status); self.assertIn('No existe un paciente registrado con el DNI ingresado',body)
    def test_tipo_manipulado(self):
        for tipo in ['99999','texto',"1 OR 1=1",'']:
            status,body,_=self.b.post(tipo=tipo)
            self.assertEqual(400,status); self.assertNotIn('Exception',body); self.assertNotIn('SQLException',body)
    def test_duplicado_activo_e_historico_permitido(self):
        status,body,_=self.b.post(dni='88100004')
        self.assertEqual(400,status); self.assertIn('ya tiene un tratamiento activo',body)
        self.assertEqual(200,self.b.post(dni='88100003')[0])
        self.assertEqual('2',sql("SELECT COUNT(*) FROM tratamientos WHERE dni_paciente='88100003';"))
    def test_csrf_y_tokens_ajenos(self):
        self.assertEqual(403,self.b.post(csrf='incorrecto')[0])
        other=Browser()
        self.assertEqual(400,other.post(token=self.b.token)[0])
        self.assertEqual(400,other.get(PATH+'?resultado='+self.b.token)[0])
    def test_concurrencia_http(self):
        forms=[Browser() for _ in range(6)]
        with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
            statuses=list(pool.map(lambda b:b.post(dni='88100005')[0],forms))
        self.assertEqual(1,statuses.count(200)); self.assertEqual(5,statuses.count(400))
        self.assertEqual('1',sql("SELECT COUNT(*) FROM tratamientos WHERE dni_paciente='88100005';"))
    def test_evento10_tratamiento_sin_cuotas(self):
        self.assertEqual(200,self.b.post(dni='88100009')[0])
        status,body,_=self.b.get('/deudas'); self.assertEqual(200,status)
        status,body,_=self.b.request('/deudas',dict(csrf=hidden(body,'csrf'),accion='buscar',dni='88100009',desde='2026-01-01',hasta='2027-12-31'))
        self.assertEqual(200,status); self.assertIn('No se encontraron cuotas pendientes',body)
        self.assertNotIn('data-cuota=',body); self.assertNotIn('Inconsistencia:',body)

if __name__=='__main__': unittest.main(verbosity=2)
