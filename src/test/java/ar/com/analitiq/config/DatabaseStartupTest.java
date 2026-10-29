package ar.com.analitiq.config;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DatabaseStartupTest {
    @TempDir Path temporary;

    @Test void mysqlAusenteNoImpideCrearLosPoolsParaReintentar() throws Exception {
        assumeTrue(System.getenv("ANALITIQ_DB_URL") == null,
                   "Este test no debe usar una conexión real del entorno.");
        // Puerto reservado sin aceptar conexiones: ningún servidor MySQL interviene.
        try (var unavailable = new ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress())) {
            var config = temporary.resolve("offline.properties");
            Files.writeString(config, "db.url=jdbc:mysql://127.0.0.1:" + unavailable.getLocalPort()
                + "/offline?sslMode=DISABLED\ndb.user=test_only\ndb.password=test_only\n"
                + "db.write.user=test_only\ndb.write.password=test_only\n");
            String previous = System.getProperty("analitiq.config");
            System.setProperty("analitiq.config", config.toString());
            try (var read = assertDoesNotThrow(Database::open);
                 var write = assertDoesNotThrow(Database::openRegistro)) {
                assertFalse(read.isClosed());
                assertFalse(write.isClosed());
            } finally {
                if (previous == null) System.clearProperty("analitiq.config");
                else System.setProperty("analitiq.config", previous);
            }
        }
    }
}
