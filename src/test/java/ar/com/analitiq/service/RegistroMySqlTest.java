package ar.com.analitiq.service;

import ar.com.analitiq.model.*;
import com.zaxxer.hikari.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/** Escrituras exclusivamente en analitiq_evento3_test, nunca en la demo del usuario. */
class RegistroMySqlTest {
    private HikariDataSource ds;
    private Connection admin;
    private RegistroTratamientoService s;
    @BeforeEach void preparar() throws Exception {
        String archivo=System.getenv("ANALITIQ_REGISTRO_TEST_CONFIG");
        assumeTrue(archivo!=null,"Requiere esquema aislado y configuración de pruebas del evento 3.");
        var p=new Properties();
        try(var reader=Files.newBufferedReader(Path.of(archivo),StandardCharsets.UTF_8)) { p.load(reader); }
        String url=p.getProperty("db.url");
        assertTrue(url.matches("jdbc:mysql://[^/]+/analitiq_evento3_test(?:\\?.*)?"),"No ejecutar escrituras de pruebas fuera del esquema aislado.");
        assumeTrue(System.getenv("ANALITIQ_TEST_ADMIN_USER")!=null,"Configurar cuenta administradora del esquema de pruebas.");
        admin=DriverManager.getConnection(url,System.getenv("ANALITIQ_TEST_ADMIN_USER"),Objects.toString(System.getenv("ANALITIQ_TEST_ADMIN_PASSWORD"),""));
        assertEquals("analitiq_evento3_test",admin.getCatalog());
        limpiar();
        try(var st=admin.createStatement()) {
            for(int i=1;i<=9;i++) st.executeUpdate("INSERT INTO pacientes VALUES ('8800000"+i+"','Prueba','EventoTres','2000-01-01',NULL,'1100000000',NULL,NULL,'prueba@example.invalid')");
        }
        var cfg=new HikariConfig(); cfg.setJdbcUrl(url); cfg.setUsername(p.getProperty("db.write.user")); cfg.setPassword(p.getProperty("db.write.password"));
        cfg.setMaximumPoolSize(8); ds=new HikariDataSource(cfg); s=new RegistroTratamientoService(ds);
    }
    private void limpiar() throws SQLException {
        try(var st=admin.createStatement()) {
            st.executeUpdate("DELETE FROM tratamientos WHERE dni_paciente BETWEEN '88000001' AND '88000009'");
            st.executeUpdate("DELETE FROM pacientes WHERE dni_paciente BETWEEN '88000001' AND '88000009'");
        }
    }
    @AfterEach void cerrar() throws Exception {
        if(ds!=null) ds.close();
        if(admin!=null) { try { limpiar(); } finally { admin.close(); } }
    }
    private DatosTratamiento datos(String dni,String tipo) { return DatosTratamiento.parse(dni,tipo,"2026-09-23","","",""); }
    private String token() { return UUID.randomUUID().toString(); }
    private long contar(String tabla) throws SQLException {
        try(var st=admin.createStatement(); var r=st.executeQuery("SELECT COUNT(*) FROM "+tabla)) { r.next(); return r.getLong(1); }
    }
    @Test void guardaObligatoriosNullsYNoCreaDeudas() throws Exception {
        long presupuestos=contar("presupuestos"),cuotas=contar("cuotas"),pagos=contar("pagos");
        var alta=s.registrar(datos("88000001","1"),token()); assertTrue(alta.getCodigo()>2506);
        try(var st=admin.prepareStatement("SELECT * FROM tratamientos WHERE codigo_tratamiento=?")) {
            st.setLong(1,alta.getCodigo()); try(var r=st.executeQuery()) {
                assertTrue(r.next()); assertEquals("SI",r.getString("activo"));
                assertNull(r.getDate("fecha_finalizacion_tratamiento_estimada")); assertNull(r.getString("objetivos")); assertNull(r.getString("pronostico"));
            }
        }
        var informe=new DeudasService(ds).consultar("88000001",new FiltrosDeuda(RangoFechas.parse("2026-01-01","2027-12-31"),""));
        assertTrue(informe.getTratamientos().isEmpty()); assertTrue(informe.getAvisos().isEmpty());
        assertEquals(presupuestos,contar("presupuestos")); assertEquals(cuotas,contar("cuotas")); assertEquals(pagos,contar("pagos"));
    }
    @Test void guardaOpcionalesCompletos() throws Exception {
        var alta=s.registrar(DatosTratamiento.parse("88000001","2","2026-09-23","2027-01-01","Mejorar función 🙂","Favorable"),token());
        try(var st=admin.prepareStatement("SELECT objetivos,pronostico,fecha_finalizacion_tratamiento_estimada FROM tratamientos WHERE codigo_tratamiento=?")) {
            st.setLong(1,alta.getCodigo()); try(var r=st.executeQuery()) {
                assertTrue(r.next()); assertEquals("Mejorar función 🙂",r.getString(1)); assertEquals("Favorable",r.getString(2)); assertEquals("2027-01-01",r.getString(3));
            }
        }
    }
    @Test void rechazaInexistentesYDuplicadosSinInsertar() throws Exception {
        long antes=contar("tratamientos");
        assertEquals("No existe un paciente registrado con el DNI ingresado",assertThrows(IllegalArgumentException.class,() -> s.registrar(datos("99999999","1"),token())).getMessage());
        assertThrows(IllegalArgumentException.class,() -> s.registrar(datos("88000001","99999"),token()));
        assertEquals(antes,contar("tratamientos"));
        s.registrar(datos("88000001","1"),token());
        assertThrows(IllegalArgumentException.class,() -> s.registrar(datos("88000001","1"),token()));
        assertEquals(antes+1,contar("tratamientos"));
    }
    @Test void permiteHistoricosYTiposDistintos() throws Exception {
        var previo=s.registrar(datos("88000001","1"),token());
        try(var st=admin.prepareStatement("UPDATE tratamientos SET activo='NO' WHERE codigo_tratamiento=?")) { st.setLong(1,previo.getCodigo()); st.executeUpdate(); }
        var nuevo=s.registrar(datos("88000001","1"),token()); assertNotEquals(previo.getCodigo(),nuevo.getCodigo());
        s.registrar(datos("88000001","2"),token()); s.registrar(datos("88000001","3"),token());
        try(var st=admin.createStatement();var r=st.executeQuery("SELECT COUNT(*) FROM tratamientos WHERE dni_paciente='88000001'")) { r.next(); assertEquals(4,r.getInt(1)); }
    }
    @Test void reenvioDevuelveMismoRegistroInclusoOtroServicio() throws Exception {
        String token=token(); var uno=s.registrar(datos("88000001","1"),token);
        assertEquals(uno.getCodigo(),new RegistroTratamientoService(ds).registrar(datos("88000001","1"),token).getCodigo());
    }
    @Test void concurrenciaMismoFormularioYFormulariosDistintos() throws Exception {
        var pool=Executors.newFixedThreadPool(8);
        try {
            String token=token(); var inicio=new CountDownLatch(1); var futuros=new ArrayList<Future<Long>>();
            for(int i=0;i<8;i++) futuros.add(pool.submit(() -> { inicio.await(); return s.registrar(datos("88000001","1"),token).getCodigo(); }));
            inicio.countDown(); var ids=new HashSet<Long>(); for(var futuro:futuros) ids.add(futuro.get(20,TimeUnit.SECONDS));
            assertEquals(1,ids.size());
            var barrera=new CountDownLatch(1); var distintos=new ArrayList<Future<Boolean>>();
            for(int i=0;i<8;i++) distintos.add(pool.submit(() -> { barrera.await(); try { s.registrar(datos("88000002","1"),token()); return true; } catch(IllegalArgumentException expected) { return false; } }));
            barrera.countDown(); int exitos=0; for(var futuro:distintos) if(futuro.get(20,TimeUnit.SECONDS)) exitos++;
            assertEquals(1,exitos);
        } finally { pool.shutdownNow(); }
    }
    @Test void permisosLimitadosEIndiceUnico() throws Exception {
        s.registrar(datos("88000001","1"),token());
        try(var c=ds.getConnection();var st=c.createStatement()) {
            assertThrows(SQLException.class,() -> st.executeUpdate("UPDATE tratamientos SET activo='NO' WHERE 1=0"));
            assertThrows(SQLException.class,() -> st.executeUpdate("DELETE FROM tratamientos WHERE 1=0"));
            assertThrows(SQLException.class,() -> st.executeUpdate("INSERT INTO pacientes SELECT * FROM pacientes WHERE 1=0"));
            var error=assertThrows(SQLException.class,() -> st.executeUpdate("INSERT INTO tratamientos(codigo_tipo_tratamiento,dni_paciente,fecha_inicio_tratamiento,activo) VALUES(1,'88000001','2026-09-23','SI')"));
            assertEquals(1062,error.getErrorCode());
        }
    }
}
