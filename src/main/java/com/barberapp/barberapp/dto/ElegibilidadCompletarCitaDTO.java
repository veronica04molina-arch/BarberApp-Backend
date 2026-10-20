package com.barberapp.barberapp.dto;

/** Resultado de la validación de finalización calculada con la hora del servidor. */
public class ElegibilidadCompletarCitaDTO {

    private final boolean puedeCompletar;
    private final String motivo;

    public ElegibilidadCompletarCitaDTO(boolean puedeCompletar, String motivo) {
        this.puedeCompletar = puedeCompletar;
        this.motivo = motivo;
    }

    public boolean isPuedeCompletar() {
        return puedeCompletar;
    }

    public String getMotivo() {
        return motivo;
    }
}
