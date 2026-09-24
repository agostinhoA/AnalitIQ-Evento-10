package ar.com.analitiq.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Database {
    private Database() {}
    public static HikariDataSource open() throws Exception {
        return open(false);
    }
    public static HikariDataSource openRegistro() throws Exception {
        return open(true);
    }
    private static HikariDataSource open(boolean registro) throws Exception {
        Properties p = new Properties();
        String file = System.getProperty("analitiq.config", System.getenv("ANALITIQ_CONFIG"));
        if (file != null && !file.isBlank()) {
            try (Reader in = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) { p.load(in); }
        }
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(value(p, "db.url", "ANALITIQ_DB_URL"));
        cfg.setUsername(value(p, registro ? "db.write.user" : "db.user", registro ? "ANALITIQ_DB_WRITE_USER" : "ANALITIQ_DB_USER"));
        cfg.setPassword(value(p, registro ? "db.write.password" : "db.password", registro ? "ANALITIQ_DB_WRITE_PASSWORD" : "ANALITIQ_DB_PASSWORD"));
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
        cfg.setPoolName(registro ? "AnalitIQ-registro" : "AnalitIQ");
        cfg.setMaximumPoolSize(10); cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(5000); cfg.setValidationTimeout(3000);
        cfg.setReadOnly(!registro);
        cfg.addDataSourceProperty("connectTimeout", "5000");
        cfg.addDataSourceProperty("socketTimeout", "15000");
        cfg.setInitializationFailTimeout(1);
        return new HikariDataSource(cfg);
    }
    private static String value(Properties p, String key, String env) {
        String value = System.getenv(env);
        if (value == null) value = p.getProperty(key);
        if (value == null || value.isBlank() || value.startsWith("REEMPLAZAR"))
            throw new IllegalStateException("Falta configuración externa: " + key);
        return value;
    }
}
