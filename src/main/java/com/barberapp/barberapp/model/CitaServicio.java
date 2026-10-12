package com.barberapp.barberapp.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cita_servicio")
public class CitaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "idcita", nullable = false)
    private Integer idCita;

    @Column(name = "idservicio", nullable = false)
    private Integer idServicio;

    @Column(name = "precio_aplicado", precision = 10, scale = 2)
    private BigDecimal precioAplicado;

    @Column(name = "descripcion_aplicada", columnDefinition = "TEXT")
    private String descripcionAplicada;

    public CitaServicio() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdCita() {
        return idCita;
    }

    public void setIdCita(Integer idCita) {
        this.idCita = idCita;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public BigDecimal getPrecioAplicado() {
        return precioAplicado;
    }

    public void setPrecioAplicado(BigDecimal precioAplicado) {
        this.precioAplicado = precioAplicado;
    }

    public String getDescripcionAplicada() {
        return descripcionAplicada;
    }

    public void setDescripcionAplicada(String descripcionAplicada) {
        this.descripcionAplicada = descripcionAplicada;
    }

}
