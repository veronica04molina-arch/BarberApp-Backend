package com.barberapp.barberapp.dto;

public class ReporteCitasDTO {

    private long totalCitas;
    private long citasPendientes;
    private long citasConfirmadas;
    private long citasCompletadas;
    private long citasCanceladas;

    public ReporteCitasDTO(
            long totalCitas,
            long citasPendientes,
            long citasConfirmadas,
            long citasCompletadas,
            long citasCanceladas) {

        this.totalCitas = totalCitas;
        this.citasPendientes = citasPendientes;
        this.citasConfirmadas = citasConfirmadas;
        this.citasCompletadas = citasCompletadas;
        this.citasCanceladas = citasCanceladas;
    }

    public ReporteCitasDTO(
            long totalCitas,
            long citasPendientes,
            long citasCanceladas) {

        this(totalCitas, citasPendientes, 0, 0, citasCanceladas);
    }

    public long getTotalCitas() {
        return totalCitas;
    }

    public long getCitasPendientes() {
        return citasPendientes;
    }

    public long getCitasConfirmadas() {
        return citasConfirmadas;
    }

    public long getCitasCompletadas() {
        return citasCompletadas;
    }

    public long getCitasCanceladas() {
        return citasCanceladas;
    }
}
