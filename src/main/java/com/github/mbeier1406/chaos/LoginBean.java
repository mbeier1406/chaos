package com.github.mbeier1406.chaos;

import java.io.Serializable;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
 * <h3>Hash-Generierung für application.properties:</h3>
 * Um einen BCrypt-Hash für das Passwort zu erzeugen, kann folgender Code verwendet werden:
 * <pre>{@code
 * String password = "meinPasswort123";
 * String hash = BcryptUtil.bcryptHash(password);
 * System.out.println("BCrypt-Hash: " + hash);
 * }</pre>
 * 
 * Der generierte Hash kann dann in der {@code application.properties} unter
 * {@code chaos.login.password} eingetragen werden.
 * 
 * @see BcryptUtil
 */
@Getter
@Setter
@Named
@SessionScoped
public class LoginBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Konfigurierter Benutzername aus application.properties */
    @Inject
    @ConfigProperty(name = "chaos.login.username")
    String configuredUsername;
    
    /** BCrypt-Hash des konfigurierten Passworts aus application.properties */
    @Inject
    @ConfigProperty(name = "chaos.login.password")
    String configuredPasswordHash;
    
    /** Vom Benutzer eingegebener Benutzername */
    private String username;
    
    /** Vom Benutzer eingegebenes Passwort (im Klartext) */
    private String password;
    
    /** Status, ob der Benutzer erfolgreich angemeldet ist */
    private boolean loggedIn = false;
    
    /** Fehlermeldung bei fehlgeschlagenem Login */
    private String errorMessage;
    
    /**
     * Authentifiziert den Benutzer anhand von Benutzername und Passwort.
     * <p>
     * Vergleicht die eingegebenen Credentials mit den konfigurierten Werten.
     * Das Passwort wird mittels BCrypt sicher verglichen.
     * </p>
     * 
     * @return Navigation-Outcome: {@code "dashboard?faces-redirect=true"} bei Erfolg,
     *         {@code null} bei Fehler (bleibt auf Login-Seite)
     */
    public String login() {
        // BCrypt-Vergleich: hasht automatisch und vergleicht
        if (configuredUsername.equals(username) && 
            BcryptUtil.matches(password, configuredPasswordHash)) {
            loggedIn = true;
            errorMessage = null;
            return "dashboard?faces-redirect=true";
        }
        errorMessage = "Ungültige Anmeldedaten!";
        password = null;
        return null;
    }
    
    /**
     * Meldet den aktuellen Benutzer ab und setzt die Session-Daten zurück.
     * 
     * @return Navigation-Outcome: {@code "index?faces-redirect=true"} (zur Startseite)
     */
    public String logout() {
        loggedIn = false;
        username = null;
        password = null;
        errorMessage = null;
        return "index?faces-redirect=true";
    }

    /* Gibt den eingelogten Benutzernamen zurück */
    public String loggedInAs() {
        return "Eingeloggt als: '" + username + "'";
    }

    /** Testmethode um BCrypt-Hash zu generieren */
    public static void main(String[] args) {
        String password = "qwe123";
        String hash = BcryptUtil.bcryptHash(password);
        System.out.println("BCrypt-Hash: " + hash);
    }

}
