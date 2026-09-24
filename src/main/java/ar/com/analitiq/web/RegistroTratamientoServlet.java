package ar.com.analitiq.web;

import ar.com.analitiq.service.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Objects;

@WebServlet("/tratamientos/registrar")
public final class RegistroTratamientoServlet extends HttpServlet {
    private RegistroSesion estado(HttpServletRequest req) {
        HttpSession session=req.getSession();
        synchronized(session) {
            if(session.getAttribute("registroSesion")==null) session.setAttribute("registroSesion",new RegistroSesion());
            return (RegistroSesion)session.getAttribute("registroSesion");
        }
    }
    private RegistroTratamientoService servicio() { return (RegistroTratamientoService)getServletContext().getAttribute("registroService"); }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        var estado=estado(req);
        if(servicio()==null) { indisponible(req,res); return; }
        try {
            String token=req.getParameter("resultado");
            if(token!=null) {
                estado.validar(token,Instant.now());
                var resultado=servicio().resultado(token);
                if(resultado==null) throw new IllegalArgumentException("No hay un registro confirmado para este formulario.");
                req.setAttribute("registro",resultado); vista(req,res,"tratamiento-registrado");
            } else {
                req.setAttribute("tokenValor",estado.nuevo(Instant.now())); formulario(req,res);
            }
        } catch(IllegalArgumentException e) { res.setStatus(400); req.setAttribute("mensaje",e.getMessage()); vista(req,res,"error"); }
        catch(SQLException e) { fallo(req,res,e); }
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        var estado=estado(req);
        for(String nombre:new String[]{"dni","tipo","inicio","fin","objetivos","pronostico","token"}) req.setAttribute(nombre+"Valor",req.getParameter(nombre));
        if(!Objects.equals(estado.getCsrf(),req.getParameter("csrf"))) {
            res.setStatus(403); req.setAttribute("mensaje","El formulario venció. Abrí un nuevo registro."); vista(req,res,"error"); return;
        }
        if(servicio()==null) { indisponible(req,res); return; }
        try {
            estado.validar(req.getParameter("token"),Instant.now());
        } catch(IllegalArgumentException e) {
            res.setStatus(400); req.setAttribute("mensaje",e.getMessage()); vista(req,res,"error"); return;
        }
        try {
            var datos=DatosTratamiento.parse(req.getParameter("dni"),req.getParameter("tipo"),req.getParameter("inicio"),req.getParameter("fin"),req.getParameter("objetivos"),req.getParameter("pronostico"));
            servicio().registrar(datos,req.getParameter("token"));
            res.setStatus(303); res.setHeader("Location",req.getContextPath()+"/tratamientos/registrar?resultado="+req.getParameter("token"));
        } catch(IllegalArgumentException e) {
            res.setStatus(400); req.setAttribute("mensaje",e.getMessage());
            try { formulario(req,res); } catch(SQLException sql) { fallo(req,res,sql); }
        } catch(SQLException e) { fallo(req,res,e); }
    }
    private void formulario(HttpServletRequest req,HttpServletResponse res) throws SQLException,ServletException,IOException {
        var tipos=servicio().catalogo();
        String enviado=(String)req.getAttribute("tipoValor");
        req.setAttribute("tipoNoDisponible",enviado!=null && !enviado.isBlank() && tipos.stream().noneMatch(t -> Long.toString(t.getCodigo()).equals(enviado)));
        req.setAttribute("tipos",tipos); vista(req,res,"registrar-tratamiento");
    }
    private void fallo(HttpServletRequest req,HttpServletResponse res,SQLException e) throws ServletException,IOException {
        getServletContext().log("AnalitIQ: fallo de registro JDBC, SQLState="+e.getSQLState());
        res.setStatus(503); req.setAttribute("mensaje","No pudimos confirmar el registro. Intentá nuevamente con este mismo formulario; si ya se guardó, no se duplicará.");
        if(req.getAttribute("tokenValor")==null) { vista(req,res,"error"); return; }
        try { req.setAttribute("tipos",servicio().catalogo()); } catch(SQLException unavailable) { req.setAttribute("tipos",java.util.List.of()); }
        req.setAttribute("tipoNoDisponible",req.getAttribute("tipoValor")!=null);
        vista(req,res,"registrar-tratamiento");
    }
    private void indisponible(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        res.setStatus(503); req.setAttribute("mensaje","El registro de tratamientos no está disponible. Revisá la conexión y la configuración de escritura."); vista(req,res,"error");
    }
    private void vista(HttpServletRequest req,HttpServletResponse res,String nombre) throws ServletException,IOException {
        req.setAttribute("seccion","registro"); req.setAttribute("tituloPagina","Registrar un tratamiento");
        req.getRequestDispatcher("/WEB-INF/views/"+nombre+".jsp").forward(req,res);
    }
}
