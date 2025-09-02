package com.github.mbeier1406.chaos;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;

@Named
@SessionScoped
public class ChaosBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String message = "Willkommen bei Chaos!";
    private String currentTime;

    @Inject
    private HttpServletResponse response;

    public ChaosBean() {
        updateTime();
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getCurrentTime() {
        updateTime();
        return currentTime;
    }
    
    private void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        this.currentTime = now.format(formatter);
    }
    
    public String navigateToDashboard() {
        return "dashboard?faces-redirect=true";
    }
    
    public void showWelcomeMessage() {
        this.message = "Sie haben den Button geklickt! Zeit: " + getCurrentTime();
    }

    public void throwError() {
        throw new RuntimeException("Test Fehler");
    }

    public void throw404Error() throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "404 - Seite nicht gefunden");
    }

}
