package ar.com.analitiq.web;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/inicio")
public final class InicioServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        req.setAttribute("seccion","inicio"); req.setAttribute("tituloPagina","Inicio");
        req.getRequestDispatcher("/WEB-INF/views/inicio.jsp").forward(req,res);
    }
}
