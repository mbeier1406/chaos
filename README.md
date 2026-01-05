# Chaos Web Application

Eine moderne Web-Anwendung basierend auf Quarkus, JSF und PrimeFaces.

## Technologie-Stack

- **Java 21** - Programmiersprache
- **Quarkus 3.24.3** - Supersonic Subatomic Java Framework (Jakarta EE 10) 
- **Quarliverse 3.15.5** - Community-Extensions für Quarkus stellt quarkus-primefaces bereit
- **myfaces-quarkus 4.1.1** - die Quarkus-Extension
- **JSF 4.1.1** - JavaServer Faces für Web-UI (Apache MyFaces aus myfaces-api)
- **PrimeFaces 15.0.5** - Rich UI Component Library
- **Maven** - Build-Tool (als Wrapper ./mvnw)

## Projektstruktur

```
chaos/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/github/mbeier1406/chaos/
│   │   │       ├── ChaosResource.java    # REST-Endpoint
│   │   │       └── ChaosBean.java        # JSF Managed Bean
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   ├── resources/
│   │       │   │   ├── index.xhtml       # Hauptseite
│   │       │   │   └── dashboard.xhtml   # Dashboard-Seite
│   │       │   ├── web.xml               # Web-Konfiguration
│   │       │   └── faces-config.xml      # JSF-Konfiguration
│   │       └── application.properties    # Quarkus-Konfiguration
│   └── test/
└── pom.xml
```

## Features

- **Responsive Design** - Moderne, mobile-freundliche Benutzeroberfläche
- **Sidebar Navigation** - Linksseitige Navigation für Menüeinträge
- **PrimeFaces Components** - Rich UI Components für bessere UX
- **Session Management** - JSF Session-Scoped Beans
- **Live Reload** - Entwicklung mit automatischem Neuladen

## Entwicklung

### Voraussetzungen

- Lunux Ubuntu 24.04
- Java 21
- Maven 3.8+

### Aufsetzen

```
$ # Projekterstellung
$ mvn io.quarkus.platform:quarkus-maven-plugin:3.6.4:create \
     -DprojectGroupId=com.github.mbeier1406 \
     -DprojectArtifactId=chaos \
     -DprojectVersion=0.0.1 \
     -DclassName="com.github.mbeier1406.chaos.ChaosResource" \
     -Dextensions="resteasy"
$ # JSF/PrimeFaces Extensions hinzufügen
$ ./mvnw quarkus:add-extension -Dextensions="myfaces-quarkus,quarkus-primefaces"
```

Weitere Dateien:
- Web-Ressourcen (`index.xhtml`, `dashboard.xhtml`)
- Konfigurationsdateien (`web.xml`, `faces-config.xml`)
- Java-Komponenten (`ChaosBean.java`)
- Anwendungseinstellungen (`application.properties`)
- Dokumentation (`README.md`)


### Anwendung starten

```bash
# Development Mode mit Live Reload über Wrapper
./mvnw quarkus:dev

# Oder mit Maven
mvn quarkus:dev
```

Die Anwendung ist dann unter `http://localhost:8080` erreichbar.

### Build

```bash
# JAR erstellen
./mvnw clean package

# Native Image erstellen (optional)
./mvnw clean package -Pnative
```

## Konfiguration

Die Anwendung kann über `src/main/resources/application.properties` konfiguriert werden:

- **Port**: Standardmäßig 8080
- **PrimeFaces Theme**: Saga
- **JSF Suffix**: .xhtml
- **Live Reload**: Aktiviert für Entwicklung

## Authentifizierungsmechanismen

Diese Anwendung demonstriert **vier verschiedene Authentifizierungsansätze** in einer JSF/Quarkus-Anwendung.

### Übersicht der Mechanismen

| Mechanismus | Ebene | Implementierung | Beispiel-Seite |
|-------------|-------|-----------------|----------------|
| **Servlet Filter** | Servlet | `AuthenticationFilter` | `/dashboard.xhtml` |
| **JSF PhaseListener** | JSF Lifecycle | `AuthenticationPhaseListener` | `/reports.xhtml` |
| **XHTML Event** | View | `<f:event type="preRenderView">` | `/user.xhtml` |
| **Container Security** | Jakarta EE | `web.xml` Security Constraints | `/properties.xhtml` |

### 1. Servlet Filter (`AuthenticationFilter`)

**Wo:** `src/main/java/com/github/mbeier1406/chaos/AuthenticationFilter.java`

- Läuft **vor** JSF auf Servlet-Ebene
- Zentrale Konfiguration über `PUBLIC_PAGES` Array
- **Vorteil:** Sehr effizient, greift früh
- **Beispiel:** `/dashboard.xhtml`

```java
@WebFilter(urlPatterns = "*.xhtml")
public class AuthenticationFilter implements Filter {
    private static final String[] PUBLIC_PAGES = {
        "/login.xhtml", "/index.xhtml", "/user.xhtml"
    };
}
```

### 2. JSF PhaseListener (`AuthenticationPhaseListener`)

**Wo:** `src/main/java/com/github/mbeier1406/chaos/AuthenticationPhaseListener.java`

- Läuft **während** des JSF-Lifecycle (RESTORE_VIEW Phase)
- Zentrale Konfiguration über `faces-config.xml`
- **Vorteil:** Zugriff auf JSF-Context
- **Beispiel:** `/reports.xhtml`

```xml
<lifecycle>
    <phase-listener>
        com.github.mbeier1406.chaos.AuthenticationPhaseListener
    </phase-listener>
</lifecycle>
```

### 3. XHTML Event Check

**Wo:** `src/main/resources/META-INF/resources/user.xhtml`

- Prüfung direkt in der XHTML-Datei
- Dezentrale Konfiguration (pro Seite)
- **Vorteil:** Sehr feingranular
- **Nachteil:** Muss auf jeder Seite wiederholt werden
- **Beispiel:** `/user.xhtml`

```xml
<f:event type="preRenderView" listener="#{loginBean.checkAuthentication}" />
```

### 4. Container Security (Jakarta EE)

**Wo:** `src/main/resources/META-INF/web.xml` + `application.properties`

- Standard Jakarta EE Container-managed Security
- Verwendet `web.xml` Security Constraints
- Unabhängig von der LoginBean-Authentifizierung
- **Beispiel:** `/properties.xhtml`

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Properties</web-resource-name>
        <url-pattern>/properties.xhtml</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>admin</role-name>
    </auth-constraint>
</security-constraint>
```

**Benutzer:** `admin` / `qwe123` (konfiguriert in `application.properties`)

### Wichtig: Zwei unabhängige Authentifizierungssysteme!

Die Anwendung verwendet **zwei separate Authentifizierungssysteme**, die sich die HTTP-Session teilen, aber **nicht die Authentifizierungsinformationen**:

#### 1. LoginBean-Authentifizierung (Custom)
- **Login:** `/login.xhtml`
- **Status:** `loginBean.isLoggedIn()`
- **Geschützte Seiten:** `/dashboard.xhtml`, `/reports.xhtml`, `/user.xhtml`
- **Credentials:** `admin` / `qwe123` (aus `application.properties`)

#### 2. Container Security (Jakarta EE)
- **Login:** Browser Basic Auth Popup
- **Status:** Quarkus `SecurityIdentity`
- **Geschützte Seiten:** `/properties.xhtml`
- **Credentials:** `admin` / `qwe123` (aus `application.properties`)

```
┌─────────────────── HTTP Session (JSESSIONID) ───────────────────┐
│                                                                   │
│  ┌──────────────────────────────────────────────────────┐       │
│  │ LoginBean (CDI SessionScoped)                        │       │
│  │ Verwendet von: Filter, PhaseListener, XHTML Events   │       │
│  └──────────────────────────────────────────────────────┘       │
│                                                                   │
│  ┌──────────────────────────────────────────────────────┐       │
│  │ Quarkus Security Context (Container)                 │       │
│  │ Verwendet von: Container Security (web.xml)          │       │
│  └──────────────────────────────────────────────────────┘       │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

**Hinweis:** In einer Produktionsanwendung sollten Sie sich für **einen** Authentifizierungsmechanismus entscheiden. Diese Demo zeigt verschiedene Ansätze zu Lernzwecken.

### Login-Credentials

- **Benutzername:** `admin`
- **Passwort:** `qwe123`
- **BCrypt Hash:** `$2a$10$9busRTAodhYPO9vbihyC0eikCNou/LZx.ysOEtW69M/Tu6LMFRiQS`

## Navigation

- **Startseite**: `http://localhost:8080/` oder `http://localhost:8080/index.xhtml`
- **Dashboard**: `http://localhost:8080/dashboard.xhtml`

## Entwicklung

### Neue Seiten hinzufügen

1. Erstellen iner neue `.xhtml` Datei in `src/main/resources/META-INF/resources/`
2. Navigation zur Sidebar hinzufügen
3. Erstellung ggf. einer neuen Managed Bean

### Debugging

HTTP-Requests und Responses anzeigen: `$ sudo ngrep -d lo -W byline '' port 8080`
