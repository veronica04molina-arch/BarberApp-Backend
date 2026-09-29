package com.barberapp.barberapp.dto;

public class ReporteServicioDTO {

    private String servicio;
    private long cantidad;

    public ReporteServicioDTO(
            String servicio,
            long cantidad) {

        this.servicio = servicio;
        this.cantidad = cantidad;
    }

    public String getServicio() {
        return servicio;
    }

    public long getCantidad() {
        return cantidad;
    }
}