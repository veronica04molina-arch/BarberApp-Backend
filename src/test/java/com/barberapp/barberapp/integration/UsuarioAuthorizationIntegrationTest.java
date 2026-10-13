package com.barberapp.barberapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

class UsuarioAuthorizationIntegrationTest extends AuthorizationIntegrationTestSupport {

    @Test
    void unClienteNoPuedeLeerNiEditarAOtroCliente() throws Exception {
        mockMvc.perform(get("/usuarios/{id}", clienteB.getId())
                .header("Authorization", autorizacion(clienteA)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/usuarios/{id}", clienteB.getId())
                .header("Authorization", autorizacion(clienteA))
                .contentType(jsonMediaType())
                .content(json(Map.of("telefono", "3001234567"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unBarberoSoloPuedeLeerClientesConUnaCitaAsignada() throws Exception {
        crearCita(clienteConCita, barberoA);

        mockMvc.perform(get("/usuarios/{id}", clienteConCita.getId())
                .header("Authorization", autorizacion(usuarioBarberoA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteConCita.getId()));

        mockMvc.perform(get("/usuarios/{id}", clienteSinCita.getId())
                .header("Authorization", autorizacion(usuarioBarberoA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void laRespuestaDeUsuarioNoExponeCamposSensibles() throws Exception {
        clienteA.setResetToken("token-secreto");
        clienteA.setResetTokenExpiration(LocalDateTime.now().plusMinutes(15));

        mockMvc.perform(get("/usuarios/{id}", clienteA.getId())
                .header("Authorization", autorizacion(clienteA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.resetToken").doesNotExist())
                .andExpect(jsonPath("$.resetTokenExpiration").doesNotExist());
    }
}
