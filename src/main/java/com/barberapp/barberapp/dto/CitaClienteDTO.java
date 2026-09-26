package com.barberapp.barberapp.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CitaClienteDTO {

    private Integer id;
    private LocalDate fecha;
    private LocalTime hora;
    private List<String> servicios;
    private String nombreBarbero;
    private String estado;
    private String notas;

    public CitaClienteDTO(
            Integer id,
            LocalDate fecha,
            LocalTime hora,
            List<String> servicios,
            String nombreBarbero,
            String estado,
            String notas) {

        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.servicios = servicios;
        this.nombreBarbero = nombreBarbero;
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

    public List<String> getServicios() {
        return servicios;
    }

    public String getNombreBarbero() {
        return nombreBarbero;
    }

    public String getEstado() {
        return estado;
    }

    public String getNotas() {
        return notas;
    }
}
