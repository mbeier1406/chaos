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

## Navigation

- **Startseite**: `http://localhost:8080/` oder `http://localhost:8080/index.xhtml`
- **Dashboard**: `http://localhost:8080/dashboard.xhtml`

## Entwicklung

### Neue Seiten hinzufügen

1. Erstellen iner neue `.xhtml` Datei in `src/main/resources/META-INF/resources/`
2. Navigation zur Sidebar hinzufügen
3. Erstellung ggf. einer neuen Managed Bean
