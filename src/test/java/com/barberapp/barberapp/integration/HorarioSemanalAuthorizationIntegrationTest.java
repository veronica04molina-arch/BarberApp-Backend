package com.barberapp.barberapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.barberapp.barberapp.model.HorarioSemanal;

class HorarioSemanalAuthorizationIntegrationTest extends AuthorizationIntegrationTestSupport {

    @Test
    void unBarberoNoPuedeCrearModificarNiEliminarHorariosDeOtroBarbero() throws Exception {
        HorarioSemanal horarioDeB = crearHorario(barberoB);

        mockMvc.perform(post("/horarios-semanales")
                .header("Authorization", autorizacion(usuarioBarberoA))
                .contentType(jsonMediaType())
                .content(json(cuerpoHorario(barberoB.getId()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/horarios-semanales/{id}", horarioDeB.getId())
                .header("Authorization", autorizacion(usuarioBarberoA))
                .contentType(jsonMediaType())
                .content(json(Map.of(
                        "diaSemana", "LUNES",
                        "horaInicio", "10:00",
                        "horaFin", "13:00",
                        "estado", "disponible"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/horarios-semanales/{id}", horarioDeB.getId())
                .header("Authorization", autorizacion(usuarioBarberoA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteYAnonimoNoPuedenCrearHorarios() throws Exception {
        mockMvc.perform(post("/horarios-semanales")
                .header("Authorization", autorizacion(clienteA))
                .contentType(jsonMediaType())
                .content(json(cuerpoHorario(barberoA.getId()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/horarios-semanales")
                .contentType(jsonMediaType())
                .content(json(cuerpoHorario(barberoA.getId()))))
                .andExpect(status().isUnauthorized());
    }
}
