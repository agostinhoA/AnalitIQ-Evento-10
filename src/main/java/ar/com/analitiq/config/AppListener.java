package ar.com.analitiq.config;

import ar.com.analitiq.service.PagosService;
import ar.com.analitiq.service.DeudasService;
import com.zaxxer.hikari.HikariDataSource;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collections;

@WebListener
public final class AppListener implements ServletContextListener {
    private HikariDataSource ds;
    @Override public void contextInitialized(ServletContextEvent event) {
        try {
            ds=Database.open();
            event.getServletContext().setAttribute("pagosService",new PagosService(ds));
            event.getServletContext().setAttribute("deudasService",new DeudasService(ds));
        } catch(Exception e) {
            // No volcar URL/credenciales al log ni a la página.
            event.getServletContext().log("AnalitIQ: revisar configuración externa y disponibilidad de MySQL. " + e.getClass().getSimpleName());
        }
    }
    @Override public void contextDestroyed(ServletContextEvent event) {
        if(ds!=null) ds.close();
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        for(Driver driver: Collections.list(DriverManager.getDrivers())) {
            if(driver.getClass().getClassLoader()==getClass().getClassLoader()) {
                try { DriverManager.deregisterDriver(driver); } catch(Exception ignored) { }
            }
        }
    }
}
