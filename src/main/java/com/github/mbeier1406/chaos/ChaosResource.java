package com.github.mbeier1406.chaos;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST-Ressource für die Chaos-Anwendung.
 * <p>
 * Diese Klasse stellt RESTful-Webservices bereit und demonstriert die
 * Integration von JAX-RS (RESTEasy) in eine Quarkus-Anwendung.
 * </p>
 * 
 * <h3>Verfügbare Endpoints:</h3>
 * <ul>
 *   <li>GET /chaos/hello - Gibt eine einfache Begrüßungsnachricht zurück</li>
 * </ul>
 * 
 * <h3>Verwendung:</h3>
 * <pre>
 * curl http://localhost:8080/chaos/hello
 * </pre>
 * 
 * @see Path
 * @see GET
 */
@Path("/hello")
public class ChaosResource {

    /**
     * Einfacher REST-Endpoint für Testzwecke.
     * <p>
     * Dieser Endpoint gibt eine einfache Textnachricht zurück und dient zur
     * Überprüfung, ob die REST-Schnittstelle korrekt funktioniert.
     * </p>
     * 
     * @return eine Begrüßungsnachricht als Plain-Text
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }
}
