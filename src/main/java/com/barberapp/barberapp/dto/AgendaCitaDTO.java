package com.barberapp.barberapp.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AgendaCitaDTO {

    private Integer id;
    private LocalDate fecha;
    private LocalTime hora;
    private String nombreCliente;
    private String servicio;
    private String estado;
    private String notas;

    public AgendaCitaDTO(
            Integer id,
            LocalDate fecha,
            LocalTime hora,
            String nombreCliente,
            String servicio,
            String estado,
            String notas) {

        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.nombreCliente = nombreCliente;
        this.servicio = servicio;
        this.estado = estado;
        this.notas = notas;
    }

    public Integer getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getServicio() {
        return servicio;
    }

    public String getEstado() {
        return estado;
    }

    public String getNotas() {
        return notas;
    }
}