package com.barberapp.barberapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

class CitaCompletarIntegrationTest extends AuthorizationIntegrationTestSupport {

    private static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private CitaServicioRepository citaServicioRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Test
    void unaSolicitudDirectaYLaElegibilidadRechazanLaMismaCitaNoTerminada() throws Exception {
        Cita cita = crearCitaConfirmadaConDuracionPendiente();

        mockMvc.perform(get("/citas/{id}/completar/elegibilidad", cita.getId())
                .header("Authorization", autorizacion(usuarioBarberoA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puedeCompletar").value(false))
                .andExpect(jsonPath("$.motivo").value(
                        "No se puede completar la cita hasta que haya transcurrido la duración programada del servicio."));

        mockMvc.perform(put("/citas/{id}/completar", cita.getId())
                .header("Authorization", autorizacion(usuarioBarberoA)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "No se puede completar la cita hasta que haya transcurrido la duración programada del servicio."));

        org.junit.jupiter.api.Assertions.assertEquals("confirmada",
                citaRepository.findById(cita.getId()).orElseThrow().getEstado());
    }

    private Cita crearCitaConfirmadaConDuracionPendiente() {
        Cita cita = crearCita(clienteConCita, barberoA);
        LocalDateTime inicio = LocalDateTime.now(ZONA_BOGOTA).minusMinutes(1);
        cita.setFecha(inicio.toLocalDate());
        cita.setHora(inicio.toLocalTime());
        cita.setEstado("confirmada");
        cita = citaRepository.save(cita);

        CitaServicio relacion = new CitaServicio();
        relacion.setIdCita(cita.getId());
        relacion.setIdServicio(crearServicio().getId());
        citaServicioRepository.save(relacion);
        return cita;
    }

    private Servicio crearServicio() {
        Servicio servicio = new Servicio();
        servicio.setIdBarbero(barberoA.getId());
        servicio.setNombre("Corte de prueba");
        servicio.setPrecio(BigDecimal.valueOf(25000));
        servicio.setDuracion(30);
        servicio.setDescripcion("Servicio para comprobar la finalización");
        servicio.setEstado("activo");
        return servicioRepository.save(servicio);
    }
}
