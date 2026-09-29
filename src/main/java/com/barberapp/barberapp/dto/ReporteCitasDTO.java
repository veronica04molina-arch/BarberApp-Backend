package com.barberapp.barberapp.dto;

public class ReporteCitasDTO {

    private long totalCitas;
    private long citasPendientes;
    private long citasCanceladas;

    public ReporteCitasDTO(
            long totalCitas,
            long citasPendientes,
            long citasCanceladas) {

        this.totalCitas = totalCitas;
        this.citasPendientes = citasPendientes;
        this.citasCanceladas = citasCanceladas;
    }

    public long getTotalCitas() {
        return totalCitas;
    }

    public long getCitasPendientes() {
        return citasPendientes;
    }

    public long getCitasCanceladas() {
        return citasCanceladas;
    }
}