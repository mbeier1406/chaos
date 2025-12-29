package com.github.mbeier1406.chaos;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;

/**
 * Managed Bean für die Benutzer-Authentifizierung.
 * <p>
 * Diese Session-Scoped Bean verwaltet den Login-/Logout-Prozess der Anwendung.
 * Die Credentials werden aus der {@code application.properties} geladen und
 * mit BCrypt gehashed verglichen.
 * </p>
 * 
 * <h3>Funktionen:</h3>
 * <ul>
 *   <li>Login mit BCrypt-Passwortvergleich</li>
 *   <li>Session-Management via {@link HttpSession}</li>
 *   <li>Cookie-basierte Persistenz des Logins</li>
 *   <li>Logging aller Login-Versuche mit Session- und Cookie-Informationen</li>
 * </ul>
 * 
 * <h3>Sicherheitsmerkmale:</h3>
 * <ul>
 *   <li>BCrypt für sichere Passwort-Hashes</li>
 *   <li>HttpOnly-Cookies (kein JavaScript-Zugriff)</li>
 *   <li>Secure-Cookies (nur über HTTPS)</li>
 *   <li>SameSite=Strict (CSRF-Schutz)</li>
 * </ul>
 * 
 * <h3>Hash-Generierung für application.properties:</h3>
 * Um einen BCrypt-Hash für das Passwort zu erzeugen, kann die {@link #main(String[])} 
 * Methode verwendet werden:
 * <pre>{@code
 * String password = "meinPasswort123";
 * String hash = BcryptUtil.bcryptHash(password);
 * System.out.println("BCrypt-Hash: " + hash);
 * }</pre>
 * 
 * Der generierte Hash kann dann in der {@code application.properties} unter
 * {@code chaos.login.password} eingetragen werden.
 * 
 * @author Martin Beier
 * @see BcryptUtil
 * @see HttpSession
 * @see Cookie
 */
@Getter
@Setter
@Named
@SessionScoped
public class LoginBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Name des Login-Cookies */
    private static final String LOGIN_COOKIE_NAME = "login";
    
    /** Gültigkeitsdauer des Login-Cookies in Sekunden (1 Stunde) */
    private static final int COOKIE_MAX_AGE = 3600;
    
    /** Konfigurierter Benutzername aus application.properties */
    @Inject
    @ConfigProperty(name = "chaos.login.username")
    String configuredUsername;
    
    /** BCrypt-Hash des konfigurierten Passworts aus application.properties */
    @Inject
    @ConfigProperty(name = "chaos.login.password")
    String configuredPasswordHash;

    /** Injizierte HttpSession für die Session-Verwaltung */
    @Inject
    private HttpSession session;

    /** Injizierte HttpServletRequest für den Zugriff auf Request-Daten und Cookies */
    @Inject
    private HttpServletRequest request;

    /** Injizierte HttpServletResponse für die Cookie-Verwaltung */
    @Inject
    private HttpServletResponse response;

    /** Vom Benutzer eingegebener Benutzername */
    private String username;
    
    /** Vom Benutzer eingegebenes Passwort (im Klartext, wird nicht persistiert) */
    private String password;
    
    /** Status, ob der Benutzer erfolgreich angemeldet ist */
    private boolean loggedIn = false;
    
    /** Fehlermeldung bei fehlgeschlagenem Login-Versuch */
    private String errorMessage;
    
    /**
     * Authentifiziert den Benutzer anhand von Benutzername und Passwort.
     * <p>
     * Vergleicht die eingegebenen Credentials mit den konfigurierten Werten.
     * Das Passwort wird mittels BCrypt sicher verglichen.
     * </p>
     * 
     * <h4>Bei erfolgreicher Authentifizierung:</h4>
     * <ul>
     *   <li>Setzt {@link #loggedIn} auf {@code true}</li>
     *   <li>Speichert den Benutzernamen in der Session</li>
     *   <li>Erstellt einen sicheren Login-Cookie (HttpOnly, Secure, SameSite=Strict)</li>
     *   <li>Leitet zur Dashboard-Seite weiter</li>
     * </ul>
     * 
     * <h4>Bei fehlgeschlagener Authentifizierung:</h4>
     * <ul>
     *   <li>Setzt eine Fehlermeldung</li>
     *   <li>Löscht das eingegebene Passwort aus Sicherheitsgründen</li>
     *   <li>Bleibt auf der Login-Seite</li>
     * </ul>
     * 
     * @return Navigation-Outcome: {@code "dashboard?faces-redirect=true"} bei Erfolg,
     *         {@code null} bei Fehler (bleibt auf Login-Seite)
     * @see BcryptUtil#matches(String, String)
     */
    public String login() {
        Log.infof("Login attempt for user: %s (sessionId=%s, created=%d, lastAccessed=%d, maxInactiveInterval=%d, attributes: [%s], cookies: [%s])",
            username, 
            session.getId(),
            session.getCreationTime(),
            session.getLastAccessedTime(),
            session.getMaxInactiveInterval(),
            Collections
                .list(session.getAttributeNames())
                .stream()
                .map(name -> name + "=" + session.getAttribute(name))
                .collect(Collectors.joining(", ")),
            request.getCookies() != null 
                ? Arrays.stream(request.getCookies())
                    .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                    .collect(Collectors.joining(", "))
                : "keine"
        );
        
        // BCrypt-Vergleich: hasht automatisch und vergleicht
        if (configuredUsername.equals(username) && 
            BcryptUtil.matches(password, configuredPasswordHash)) {
            loggedIn = true;
            errorMessage = null;
            session.setAttribute("username", username);
            
            // Sicheren Login-Cookie erstellen
            Cookie cookie = new Cookie(LOGIN_COOKIE_NAME, username);
            cookie.setMaxAge(COOKIE_MAX_AGE);
            cookie.setPath("/");
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);
            
            Log.infof("Login successful for user: %s", username);
            return "dashboard?faces-redirect=true";
        }
        
        Log.warnf("Login failed for user: %s", username);
        errorMessage = "Ungültige Anmeldedaten!";
        password = null;
        return null;
    }
    
    /**
     * Meldet den aktuellen Benutzer ab und setzt alle Session-Daten zurück.
     * <p>
     * Diese Methode führt folgende Aktionen aus:
     * </p>
     * <ul>
     *   <li>Setzt den Login-Status auf {@code false}</li>
     *   <li>Löscht Benutzername, Passwort und Fehlermeldung</li>
     *   <li>Entfernt den Benutzernamen aus der Session</li>
     *   <li>Invalidiert den Login-Cookie (setzt MaxAge auf 0)</li>
     * </ul>
     * 
     * @return Navigation-Outcome: {@code "index?faces-redirect=true"} (zur Startseite)
     */
    public String logout() {
        Log.infof("Logout for user: %s", username);
        loggedIn = false;
        username = null;
        password = null;
        errorMessage = null;
        session.removeAttribute("username");
        
        // Login-Cookie invalidieren
        Cookie cookie = new Cookie(LOGIN_COOKIE_NAME, "");
        cookie.setMaxAge(0);  // Cookie sofort löschen
        cookie.setPath("/");
        response.addCookie(cookie);
        
        return "index?faces-redirect=true";
    }

    /**
     * Gibt eine formatierte Anzeige des eingeloggten Benutzernamens zurück.
     * <p>
     * Diese Methode wird in der UI verwendet, um den aktuell angemeldeten
     * Benutzer anzuzeigen.
     * </p>
     * 
     * @return Formatierter String im Format "Eingeloggt als: 'benutzername'"
     */
    public String loggedInAs() {
        return "Eingeloggt als: '" + username + "'";
    }

    /**
     * Hilfsmethode zur Generierung von BCrypt-Passwort-Hashes.
     * <p>
     * Diese Methode kann direkt ausgeführt werden, um einen BCrypt-Hash
     * für die {@code application.properties} zu generieren.
     * </p>
     * 
     * <h4>Verwendung:</h4>
     * <pre>{@code
     * # Im Terminal ausführen:
     * mvn exec:java -Dexec.mainClass="com.github.mbeier1406.chaos.LoginBean"
     * }</pre>
     * 
     * @param args Kommandozeilenargumente (werden ignoriert)
     */
    public static void main(String[] args) {
        String password = "qwe123";
        String hash = BcryptUtil.bcryptHash(password);
        System.out.println("BCrypt-Hash: " + hash);
    }

}
