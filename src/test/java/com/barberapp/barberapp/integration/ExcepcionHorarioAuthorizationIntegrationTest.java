package com.barberapp.barberapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.barberapp.barberapp.model.ExcepcionHorario;

class ExcepcionHorarioAuthorizationIntegrationTest extends AuthorizationIntegrationTestSupport {

    @Test
    void unBarberoNoPuedeCrearModificarNiEliminarExcepcionesDeOtroBarbero() throws Exception {
        ExcepcionHorario excepcionDeB = crearExcepcion(barberoB);

        mockMvc.perform(post("/excepciones-horario")
                .header("Authorization", autorizacion(usuarioBarberoA))
                .contentType(jsonMediaType())
                .content(json(cuerpoExcepcion(barberoB.getId()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/excepciones-horario/{id}", excepcionDeB.getId())
                .header("Authorization", autorizacion(usuarioBarberoA))
                .contentType(jsonMediaType())
                .content(json(Map.of("tipo", "NO_DISPONIBLE", "motivo", "Cambio"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/excepciones-horario/{id}", excepcionDeB.getId())
                .header("Authorization", autorizacion(usuarioBarberoA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteYAnonimoNoPuedenCrearExcepciones() throws Exception {
        mockMvc.perform(post("/excepciones-horario")
                .header("Authorization", autorizacion(clienteA))
                .contentType(jsonMediaType())
                .content(json(cuerpoExcepcion(barberoA.getId()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/excepciones-horario")
                .contentType(jsonMediaType())
                .content(json(cuerpoExcepcion(barberoA.getId()))))
                .andExpect(status().isUnauthorized());
    }
}
