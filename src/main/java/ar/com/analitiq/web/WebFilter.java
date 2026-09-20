package ar.com.analitiq.web;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

@javax.servlet.annotation.WebFilter("/*")
public final class WebFilter implements Filter {
    @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,ServletException {
        req.setCharacterEncoding("UTF-8"); res.setCharacterEncoding("UTF-8");
        HttpServletResponse r=(HttpServletResponse)res;
        r.setHeader("Cache-Control","no-store");
        r.setHeader("X-Content-Type-Options","nosniff");
        r.setHeader("Referrer-Policy","same-origin");
        r.setHeader("Content-Security-Policy","default-src 'self'; style-src 'self'; img-src 'self'; form-action 'self'; frame-ancestors 'none'; base-uri 'none'");
        chain.doFilter(req,res);
    }
}
