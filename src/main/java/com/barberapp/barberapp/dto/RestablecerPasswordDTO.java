package com.barberapp.barberapp.dto;

public class RestablecerPasswordDTO {
    private String email; // O token según prefieras el flujo
    private String token;
    private String nuevaPassword;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNuevaPassword() { return nuevaPassword; }
    public void setNuevaPassword(String nuevaPassword) { this.nuevaPassword = nuevaPassword; }
}