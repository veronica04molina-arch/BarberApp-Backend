package com.barberapp.barberapp.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class AgendaAuthorizationIntegrationTest extends AuthorizationIntegrationTestSupport {

    @Test
    void unBarberoNoPuedeConsultarLaAgendaDeOtroBarbero() throws Exception {
        mockMvc.perform(get("/agenda/barbero/{idBarbero}", barberoB.getId())
                .header("Authorization", autorizacion(usuarioBarberoA)))
                .andExpect(status().isForbidden());
    }
}
