package com.barberapp.barberapp.dto;

public class LoginResponse {

    private String token;
    private String tipo;
    private Integer id;
    private String nombre;
    private String email;
    private String rol;

    public LoginResponse(
            String token,
            Integer id,
            String nombre,
            String email,
            String rol) {

        this.token = token;
        this.tipo = "Bearer";
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}