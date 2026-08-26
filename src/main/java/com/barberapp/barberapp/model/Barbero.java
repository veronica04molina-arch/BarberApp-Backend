package com.barberapp.barberapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "barbero")
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "idusuario")
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String especialidad;

    @Column
    private Integer experiencia;

    @Column(nullable = false, length = 20)
    private String estado = "activo";

    // Constructor vacío
    public Barbero() {
    }

    // Constructor con parámetros
    public Barbero(Usuario usuario, String especialidad,
                Integer experiencia, String estado) {
        this.usuario = usuario;
        this.especialidad = especialidad;
        this.experiencia = experiencia;
        this.estado = estado;
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Integer getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(Integer experiencia) {
        this.experiencia = experiencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Barbero{" +
                "id=" + id +
                ", usuario=" + usuario +
                ", especialidad='" + especialidad + '\'' +
                ", experiencia=" + experiencia +
                ", estado='" + estado + '\'' +
                '}';
    }
}
