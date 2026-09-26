package com.barberapp.barberapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "idusuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "idcita", nullable = false)
    private Integer idCita;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private String estado = "pendiente";

    @Column(name = "fechaenvio", nullable = false)
    private LocalDateTime fechaEnvio;

    public Notificacion() {
    }

    public Notificacion(Integer idUsuario, String mensaje) {
        this.idUsuario = idUsuario;
        this.mensaje = mensaje;
        this.estado = "pendiente";
        this.fechaEnvio = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdCita() {
        return idCita;
    }

    public void setIdCita(Integer idCita) {
        this.idCita = idCita;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}