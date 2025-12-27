package com.github.mbeier1406.chaos;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Haupt-Managed-Bean der Chaos-Anwendung.
 * <p>
 * Diese Session-Scoped Bean stellt verschiedene Funktionalitäten für die
 * Benutzeroberfläche bereit, darunter Zeitanzeige, Navigation und
 * Demonstrationsfunktionen für Fehlerbehandlung.
 * </p>
 * 
 * <h3>Hauptfunktionen:</h3>
 * <ul>
 *   <li>Anzeige von aktueller Zeit und Begrüßungsnachrichten</li>
 *   <li>Navigation zwischen verschiedenen Views</li>
 *   <li>Test-Funktionen für Error-Handling</li>
 * </ul>
 * 
 * @see SessionScoped
 * @see Serializable
 */
@Named
@SessionScoped
public class ChaosBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Nachricht für den Benutzer, die auf der Oberfläche angezeigt wird */
    private String message = "Willkommen bei Chaos!";
    
    /** Formatierte aktuelle Zeit */
    private String currentTime;

    /** Injizierte HttpServletResponse für direkte HTTP-Fehlerbehandlung */
    @Inject
    private HttpServletResponse response;

    /**
     * Konstruktor der Bean.
     * <p>
     * Initialisiert die Bean und aktualisiert die Zeit beim Start.
     * </p>
     */
    public ChaosBean() {
        updateTime();
    }
    
    /**
     * Gibt die aktuelle Benutzernachricht zurück.
     * 
     * @return die anzuzeigende Nachricht
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Setzt eine neue Benutzernachricht.
     * 
     * @param message die neue Nachricht
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Gibt die aktuelle Zeit formatiert zurück.
     * <p>
     * Die Zeit wird bei jedem Aufruf neu berechnet, um immer den
     * aktuellsten Wert zu liefern.
     * </p>
     * 
     * @return die aktuelle Zeit im Format "dd.MM.yyyy HH:mm:ss"
     */
    public String getCurrentTime() {
        updateTime();
        return currentTime;
    }
    
    /**
     * Aktualisiert die interne Zeit-Variable mit dem aktuellen Zeitstempel.
     * <p>
     * Die Zeit wird im deutschen Format (dd.MM.yyyy HH:mm:ss) formatiert.
     * </p>
     */
    private void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        this.currentTime = now.format(formatter);
    }
    
    /**
     * Navigiert zum Dashboard.
     * 
     * @return Navigation-Outcome zum Dashboard mit Redirect
     */
    public String navigateToDashboard() {
        return "dashboard?faces-redirect=true";
    }
    
    /**
     * Zeigt eine Willkommensnachricht mit der aktuellen Zeit an.
     * <p>
     * Diese Methode wird typischerweise durch einen Button-Klick ausgelöst
     * und demonstriert die Interaktion mit der Bean.
     * </p>
     */
    public void showWelcomeMessage() {
        this.message = "Sie haben den Button geklickt! Zeit: " + getCurrentTime();
    }

    /**
     * Test-Methode zum Auslösen eines Runtime-Fehlers.
     * <p>
     * Diese Methode dient zur Demonstration der Fehlerbehandlung und sollte
     * nur zu Testzwecken verwendet werden.
     * </p>
     * 
     * @throws RuntimeException immer, um die Fehlerbehandlung zu testen
     */
    public void throwError() {
        throw new RuntimeException("Test Fehler");
    }

    /**
     * Test-Methode zum Auslösen eines HTTP 404-Fehlers.
     * <p>
     * Diese Methode demonstriert die Verwendung von HttpServletResponse
     * für direkte HTTP-Fehlerbehandlung.
     * </p>
     * 
     * @throws IOException wenn ein I/O-Fehler beim Senden der Fehlerseite auftritt
     */
    public void throw404Error() throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "404 - Seite nicht gefunden");
    }

}
