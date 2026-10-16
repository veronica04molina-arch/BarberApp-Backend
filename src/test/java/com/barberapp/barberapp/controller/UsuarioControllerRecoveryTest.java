package com.barberapp.barberapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.barberapp.barberapp.dto.SolicitarRecuperacionDTO;
import com.barberapp.barberapp.service.UsuarioService;

import static org.mockito.Mockito.mock;

class UsuarioControllerRecoveryTest {

    @Test
    void entregaLaMismaRespuestaGenericaAunqueNoSePuedaProcesarElCorreo() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(usuarioService);
        SolicitarRecuperacionDTO dto = new SolicitarRecuperacionDTO();
        dto.setEmail("no-existe@ejemplo.com");

        ResponseEntity<?> respuestaNormal = controller.solicitarRecuperacion(dto);
        doThrow(new RuntimeException("correo no registrado"))
                .when(usuarioService).generarTokenRecuperacion(dto.getEmail());
        ResponseEntity<?> respuestaConError = controller.solicitarRecuperacion(dto);

        assertEquals(200, respuestaNormal.getStatusCode().value());
        assertEquals(respuestaNormal.getBody(), respuestaConError.getBody());
        assertEquals(Map.of("message",
                "Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña."),
                respuestaNormal.getBody());
    }
}
