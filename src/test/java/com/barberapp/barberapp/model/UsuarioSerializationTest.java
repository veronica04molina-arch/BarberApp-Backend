package com.barberapp.barberapp.model;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class UsuarioSerializationTest {

    @Test
    void noSerializaLosTokensDeRecuperacion() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setResetToken("secreto-de-recuperacion");
        usuario.setResetTokenExpiration(LocalDateTime.of(2026, 10, 12, 12, 0));

        String json = new ObjectMapper().writeValueAsString(usuario);

        assertFalse(json.contains("resetToken"));
        assertFalse(json.contains("resetTokenExpiration"));
        assertFalse(json.contains("secreto-de-recuperacion"));
    }
}
