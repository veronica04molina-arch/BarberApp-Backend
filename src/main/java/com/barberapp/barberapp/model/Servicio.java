package com.barberapp.barberapp.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "idbarbero")
    private Integer idBarbero;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer duracion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    // Constructor vacío
    public Servicio() {
    }

    // Constructor con parámetros
    public Servicio(Integer idBarbero, String nombre, BigDecimal precio,
                    Integer duracion, String descripcion, String estado) {
        this.idBarbero = idBarbero;
        this.nombre = nombre;
        this.precio = precio;
        this.duracion = duracion;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdBarbero() {
        return idBarbero;
    }

    public void setIdBarbero(Integer idBarbero) {
        this.idBarbero = idBarbero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    @Override
public String toString() {
    return "Servicio{" +
            "id=" + id +
            ", idBarbero=" + idBarbero +
            ", nombre='" + nombre + '\'' +
            ", precio=" + precio +
            ", duracion=" + duracion +
            ", descripcion='" + descripcion + '\'' +
            ", estado='" + estado + '\'' +
            '}';
}
}