package com.github.mbeier1406.chaos;

import java.io.IOException;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet-Filter für die Zugriffskontrolle auf geschützte Seiten.
 * <p>
 * Dieser Filter prüft bei jedem Request auf eine XHTML-Seite, ob der Benutzer
 * authentifiziert ist. Ist dies nicht der Fall und die angeforderte Seite ist
 * nicht in der Whitelist (öffentliche Seiten), wird der Benutzer zur Login-Seite
 * umgeleitet.
 * </p>
 * 
 * <h3>Funktionsweise:</h3>
 * <ul>
 *   <li>JSF-Ressourcen (CSS, JavaScript, Bilder) werden immer durchgelassen</li>
 *   <li>Öffentliche Seiten aus der Whitelist werden nicht geschützt</li>
 *   <li>Alle anderen Seiten erfordern eine Authentifizierung</li>
 * </ul>
 * 
 * <h3>Öffentliche Seiten konfigurieren:</h3>
 * Um eine neue Seite öffentlich zugänglich zu machen, fügen Sie den Pfad zur
 * {@link #PUBLIC_PAGES} Konstante hinzu.
 * 
 * @see LoginBean
 * @see WebFilter
 */
@WebFilter(urlPatterns = "*.xhtml")
public class AuthenticationFilter implements Filter {
    
    /** Injizierte LoginBean zur Prüfung des Authentifizierungsstatus */
    @Inject
    private LoginBean loginBean;
    
    /**
     * Whitelist der öffentlich zugänglichen Seiten.
     * <p>
     * Diese Seiten können ohne Authentifizierung aufgerufen werden.
     * Neue öffentliche Seiten können hier hinzugefügt werden.
     * </p>
     */
    private static final String[] PUBLIC_PAGES = {
        "/login.xhtml",      // Login-Seite
        "/index.xhtml",      // Startseite
        "/user.xhtml",       // Demonstriert, wie eine Seite per direktem Check in xhtml geschützt werden kann
        "/error/"            // Fehlerseiten
    };
    
    /**
     * Filtert HTTP-Requests und prüft die Zugriffsberechtigung.
     * <p>
     * Ablauf der Filterung:
     * </p>
     * <ol>
     *   <li>JSF-Ressourcen werden immer durchgelassen (CSS, JS, Images)</li>
     *   <li>Öffentliche Seiten aus der Whitelist werden durchgelassen</li>
     *   <li>Geschützte Seiten erfordern eine Authentifizierung</li>
     *   <li>Nicht authentifizierte Zugriffe werden zur Login-Seite umgeleitet</li>
     * </ol>
     * 
     * @param request  der eingehende ServletRequest
     * @param response der ausgehende ServletResponse
     * @param chain    die FilterChain für die Weitergabe des Requests
     * @throws IOException      bei I/O-Fehlern
     * @throws ServletException bei Servlet-Fehlern
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String requestPath = req.getRequestURI();

        // JSF-Ressourcen (CSS, JS, Images) IMMER durchlassen
        // Diese werden über spezielle Resource-Handler URLs ausgeliefert
        if (requestPath.contains("/jakarta.faces.resource/") || 
            requestPath.contains("/javax.faces.resource/") ||
            requestPath.contains("/resources/")) {
                chain.doFilter(request, response);
                return;
        }

        // Prüfen ob die angeforderte Seite öffentlich zugänglich ist
        boolean isPublicPage = false;
        for (String page : PUBLIC_PAGES)
            if (requestPath.contains(page)) {
                isPublicPage = true;
                break;
            }
        
        // Zugriffskontrolle: Geschützte Seiten erfordern Authentifizierung
        if (!isPublicPage && !loginBean.isLoggedIn()) {
            // Umleitung zur Login-Seite
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }
        
        // Request durchlassen - Benutzer ist authentifiziert oder Seite ist öffentlich
        chain.doFilter(request, response);
    }
}
