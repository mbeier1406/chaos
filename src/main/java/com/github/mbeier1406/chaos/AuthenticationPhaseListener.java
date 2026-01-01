package com.github.mbeier1406.chaos;

import io.quarkus.logging.Log;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

/**
 * JSF PhaseListener für die Zugriffskontrolle auf geschützte Seiten.
 * <p>
 * Dieser PhaseListener wird während des JSF-Lifecycle in der RESTORE_VIEW-Phase
 * aufgerufen und prüft, ob der Benutzer authentifiziert ist. Dies ist eine 
 * Alternative zum {@link AuthenticationFilter}, der auf Servlet-Ebene arbeitet.
 * </p>
 * 
 * <h3>Unterschiede zu anderen Authentifizierungsmechanismen:</h3>
 * <ul>
 *   <li><b>AuthenticationFilter:</b> Arbeitet vor JSF auf Servlet-Ebene (effizienter, 
 *       zentrale Konfiguration)</li>
 *   <li><b>PhaseListener:</b> Arbeitet innerhalb des JSF-Lifecycle (diese Klasse, 
 *       zentrale Konfiguration via faces-config.xml)</li>
 *   <li><b>Direct XHTML Check:</b> Prüfung per {@code <f:event type="preRenderView">} 
 *       in jeder einzelnen Seite (dezentral)</li>
 * </ul>
 * 
 * <h3>Funktionsweise:</h3>
 * <ol>
 *   <li>Der PhaseListener wird bei jeder XHTML-Anfrage in der RESTORE_VIEW-Phase aufgerufen</li>
 *   <li>Die {@link LoginBean} wird aus dem CDI-Container für die aktuelle Session geholt</li>
 *   <li>Wenn der Benutzer nicht eingeloggt ist und die Seite nicht öffentlich ist, 
 *       erfolgt eine Navigation zur Login-Seite</li>
 * </ol>
 * 
 * <h3>CDI und Session-Scoped Beans:</h3>
 * <p>
 * Der PhaseListener ist <b>kein CDI-Bean</b>, daher funktioniert {@code @Inject} nicht.
 * Die {@link LoginBean} wird stattdessen manuell über {@code CDI.current().select()} geholt.
 * CDI ist dabei "context-aware" und kennt automatisch die richtige HTTP-Session:
 * </p>
 * <ul>
 *   <li>Der PhaseListener läuft innerhalb eines HTTP-Requests</li>
 *   <li>Der {@link FacesContext} enthält alle Request-Informationen (inkl. Session)</li>
 *   <li>CDI nutzt den aktuellen Thread-lokalen Request-Kontext</li>
 *   <li>Die richtige SessionScoped LoginBean-Instanz wird automatisch gefunden</li>
 * </ul>
 * 
 * <h3>Konfiguration:</h3>
 * <p>
 * Der PhaseListener wird in der {@code faces-config.xml} registriert:
 * </p>
 * <pre>{@code
 * <lifecycle>
 *     <phase-listener>
 *         com.github.mbeier1406.chaos.AuthenticationPhaseListener
 *     </phase-listener>
 * </lifecycle>
 * }</pre>
 * 
 * <h3>Navigation:</h3>
 * <p>
 * Die Umleitung zur Login-Seite erfolgt über JSF-Navigation mit dem Outcome "login".
 * Die zugehörige Navigation-Rule muss in der {@code faces-config.xml} definiert sein:
 * </p>
 * <pre>{@code
 * <navigation-case>
 *     <from-outcome>login</from-outcome>
 *     <to-view-id>/login.xhtml</to-view-id>
 *     <redirect />
 * </navigation-case>
 * }</pre>
 * 
 * @author Martin Beier
 * @see PhaseListener
 * @see LoginBean
 * @see AuthenticationFilter
 * @see CDI
 */
public class AuthenticationPhaseListener implements PhaseListener {

    /**
     * Wird nach der RESTORE_VIEW-Phase aufgerufen und prüft die Authentifizierung.
     * <p>
     * Diese Methode führt die Zugriffskontrolle durch:
     * </p>
     * <ol>
     *   <li>Ermittelt die aufgerufene View-ID (z.B. "/dashboard.xhtml")</li>
     *   <li>Holt die {@link LoginBean} aus dem CDI-Container für die aktuelle Session</li>
     *   <li>Prüft, ob der Benutzer eingeloggt ist</li>
     *   <li>Prüft, ob die Seite öffentlich zugänglich ist</li>
     *   <li>Leitet bei fehlender Authentifizierung zur Login-Seite weiter</li>
     * </ol>
     * 
     * <h4>Spezialfall /reports.xhtml:</h4>
     * Die Seite {@code /reports.xhtml} ist technisch als öffentlich markiert, 
     * wird aber durch diesen PhaseListener explizit geschützt, um zu demonstrieren,
     * wie bestimmte Seiten gezielt über den PhaseListener abgesichert werden können.
     * 
     * @param event das PhaseEvent, das Informationen über die aktuelle Phase enthält
     */
    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        String viewId = context.getViewRoot().getViewId();
        Log.infof("AuthenticationPhaseListener: viewId: %s", viewId);
        // LoginBean aus CDI-Kontext holen (nicht injecten, da PhaseListener kein CDI-Bean ist!)
        // PhaseListener kennt die richtige Session: er läuft innerhalb eines HTTP-Requests
        // Der FacesContext enthält alle Request-Informationen (inkl. Session)
        // CDI nutzt automatisch den aktuellen Thread-lokalen Request-Kontext
        LoginBean loginBean = CDI.current().select(LoginBean.class).get();
        if ( !loginBean.isLoggedIn() && (
                // "/reports.xhtml" ist public, hier wird aber das Login erzwungen
                !new AuthenticationFilter().isPublicPage(viewId) || viewId.equals("/reports.xhtml")
            ) ) {
            var navigationHandler = context.getApplication().getNavigationHandler();
            navigationHandler.handleNavigation(context, null, "login");
        }
    }

    /**
     * Wird vor der RESTORE_VIEW-Phase aufgerufen.
     * <p>
     * Diese Implementierung führt keine Aktion aus, da die Authentifizierungsprüfung
     * erst nach dem Wiederherstellen der View sinnvoll ist.
     * </p>
     * 
     * @param event das PhaseEvent, das Informationen über die aktuelle Phase enthält
     */
    @Override
    public void beforePhase(PhaseEvent event) {
        // Keine Aktion erforderlich
    }

    /**
     * Gibt die Phase zurück, in der dieser Listener aktiv sein soll.
     * <p>
     * Dieser Listener ist für die {@link PhaseId#RESTORE_VIEW RESTORE_VIEW}-Phase
     * konfiguriert, die erste Phase im JSF-Lifecycle. Dies stellt sicher, dass die
     * Authentifizierung geprüft wird, bevor weitere Verarbeitungsschritte erfolgen.
     * </p>
     * 
     * @return {@link PhaseId#RESTORE_VIEW} - die Phase, in der dieser Listener aktiv ist
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

}
