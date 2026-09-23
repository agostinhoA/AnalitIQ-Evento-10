package ar.com.analitiq.web;

import ar.com.analitiq.model.*;
import ar.com.analitiq.service.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

/** /pagos conserva enlaces previos, pero ejecuta exclusivamente el evento de deudas. */
@WebServlet(urlPatterns={"/deudas","/pagos"},loadOnStartup=1)
public final class DeudasServlet extends HttpServlet {
    private BusquedaSesion estado(HttpServletRequest req) {
        HttpSession session=req.getSession();
        synchronized(session) {
            if(session.getAttribute("busquedasDeudas")==null) {
                session.setAttribute("busquedasDeudas",new BusquedaSesion());
                session.setAttribute("csrf",UUID.randomUUID().toString());
            }
            return (BusquedaSesion)session.getAttribute("busquedasDeudas");
        }
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        BusquedaSesion busquedas=estado(req);
        String id=req.getParameter("busqueda");
        if(id==null) { vista(req,res,"buscar"); return; }
        try {
            BusquedaSesion.Flujo flujo=busquedas.obtener(id,Instant.now());
            contexto(req,id,flujo);
            if(flujo.getPacientes().isEmpty()) {
                req.setAttribute("mensaje","No se encontraron pacientes");
                vista(req,res,"buscar");
            } else if(flujo.getDni()==null) vista(req,res,"seleccionar");
            else mostrarInforme(req,res,flujo);
        } catch(IllegalArgumentException e) { error(req,res,400,e.getMessage()); }
        catch(SQLException e) { dbError(req,res,e); }
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        BusquedaSesion busquedas=estado(req);
        if(!Objects.equals(req.getSession().getAttribute("csrf"),req.getParameter("csrf"))) {
            error(req,res,403,"El formulario venció. Volvé a abrir la búsqueda."); return;
        }
        String action=req.getParameter("accion");
        valoresEnviados(req);
        try {
            String id;
            if("buscar".equals(action)) {
                FiltrosDeuda filtros=filtrosEnviados(req);
                CriterioBusqueda criterio=CriterioBusqueda.validarEntrada(req.getParameter("dni"),req.getParameter("nombre"),req.getParameter("apellido"));
                DeudasService service=obtenerServicio(req,res);
                if(service==null) return;
                id=busquedas.agregar(service.buscar(criterio),filtros,criterio,Instant.now());
            } else if("seleccionar".equals(action)) {
                id=req.getParameter("busqueda");
                // El rango se toma del servidor, no de campos alterables del formulario.
                busquedas.seleccionar(id,req.getParameter("dni"),Instant.now());
            } else if("filtrar".equals(action)) {
                id=busquedas.filtrar(req.getParameter("busqueda"),filtrosEnviados(req),Instant.now());
            } else throw new IllegalArgumentException("Solicitud no válida. Iniciá una nueva búsqueda.");
            res.setStatus(303);
            res.setHeader("Location",req.getContextPath()+"/deudas?busqueda="+id);
        } catch(IllegalArgumentException e) {
            res.setStatus(400); req.setAttribute("mensaje",e.getMessage());
            if("buscar".equals(action)) vista(req,res,"buscar");
            else if("seleccionar".equals(action) || "filtrar".equals(action)) {
                try {
                    String id=req.getParameter("busqueda");
                    var flujo=busquedas.obtener(id,Instant.now());
                    contexto(req,id,flujo);
                    if("filtrar".equals(action) && flujo.getDni()!=null) {
                        valoresEnviados(req);
                        req.setAttribute("mensaje",e.getMessage()+" No se aplicó el filtro; se mantienen los criterios del informe mostrado.");
                        mostrarInforme(req,res,flujo);
                    } else vista(req,res,"seleccionar");
                } catch(IllegalArgumentException expired) { error(req,res,400,expired.getMessage()); }
                catch(SQLException database) { dbError(req,res,database); }
            } else vista(req,res,"error");
        } catch(SQLException e) { dbError(req,res,e); }
    }
    private void contexto(HttpServletRequest req,String id,BusquedaSesion.Flujo flujo) {
        req.setAttribute("busquedaId",id); req.setAttribute("flujo",flujo);
        req.setAttribute("desdeValor",flujo.getRango().getDesde().toString());
        req.setAttribute("hastaValor",flujo.getRango().getHasta().toString());
        req.setAttribute("tratamientoValor",flujo.getFiltros().getTratamiento());
        req.setAttribute("dniValor",flujo.getCriterio().dni());
        req.setAttribute("nombreValor",flujo.getCriterio().nombre());
        req.setAttribute("apellidoValor",flujo.getCriterio().apellido());
    }
    private void valoresEnviados(HttpServletRequest req) {
        req.setAttribute("desdeValor",req.getParameter("desde"));
        req.setAttribute("hastaValor",req.getParameter("hasta"));
        req.setAttribute("tratamientoValor",req.getParameter("tratamiento"));
        req.setAttribute("dniValor",req.getParameter("dni"));
        req.setAttribute("nombreValor",req.getParameter("nombre"));
        req.setAttribute("apellidoValor",req.getParameter("apellido"));
    }
    private FiltrosDeuda filtrosEnviados(HttpServletRequest req) {
        return new FiltrosDeuda(RangoFechas.parse(req.getParameter("desde"),req.getParameter("hasta")),req.getParameter("tratamiento"));
    }
    private void mostrarInforme(HttpServletRequest req,HttpServletResponse res,BusquedaSesion.Flujo flujo)
            throws SQLException,ServletException,IOException {
        DeudasService service=obtenerServicio(req,res);
        if(service==null) return;
        req.setAttribute("informe",service.consultar(flujo.getDni(),flujo.getFiltros()));
        vista(req,res,"informe");
    }
    private DeudasService obtenerServicio(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        DeudasService s=(DeudasService)getServletContext().getAttribute("deudasService");
        if(s==null) error(req,res,503,"La conexión con la base de datos no está disponible. Revisá la configuración e intentá nuevamente.");
        return s;
    }
    private void dbError(HttpServletRequest req,HttpServletResponse res,SQLException e) throws ServletException,IOException {
        getServletContext().log("AnalitIQ: fallo de lectura JDBC, SQLState="+e.getSQLState());
        error(req,res,503,"No pudimos consultar las deudas. Verificá que MySQL esté disponible e intentá nuevamente.");
    }
    private void error(HttpServletRequest req,HttpServletResponse res,int status,String mensaje) throws ServletException,IOException {
        res.setStatus(status); req.setAttribute("mensaje",mensaje); vista(req,res,"error");
    }
    private void vista(HttpServletRequest req,HttpServletResponse res,String nombre) throws ServletException,IOException {
        req.getRequestDispatcher("/WEB-INF/views/"+nombre+".jsp").forward(req,res);
    }
}
