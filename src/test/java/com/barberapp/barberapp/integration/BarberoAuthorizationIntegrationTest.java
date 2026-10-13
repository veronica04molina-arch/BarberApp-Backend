package com.barberapp.barberapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;

class BarberoAuthorizationIntegrationTest extends AuthorizationIntegrationTestSupport {

    @Test
    void unBarberoNoPuedeEditarNiDesactivarElPerfilDeOtroBarbero() throws Exception {
        mockMvc.perform(put("/api/barbero/{id}", barberoB.getId())
                .header("Authorization", autorizacion(usuarioBarberoA))
                .contentType(jsonMediaType())
                .content(json(Map.of("especialidad", "Color", "experiencia", 3))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/barbero/{id}/desactivar", barberoB.getId())
                .header("Authorization", autorizacion(usuarioBarberoA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unBarberoNoPuedeCrearUnPerfilParaOtroUsuario() throws Exception {
        mockMvc.perform(post("/api/barbero")
                .header("Authorization", autorizacion(usuarioBarberoA))
                .contentType(jsonMediaType())
                .content(json(Map.of(
                        "usuario", Map.of("id", usuarioBarberoB.getId()),
                        "especialidad", "Color",
                        "experiencia", 3,
                        "estado", "activo"))))
                .andExpect(status().isForbidden());
    }
}
