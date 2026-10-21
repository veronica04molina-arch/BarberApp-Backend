package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServicePasswordRecoveryTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private BarberoRepository barberoRepository;
    @Mock private CitaRepository citaRepository;
    @Mock private SesionService sesionService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, emailService,
                barberoRepository, citaRepository, sesionService);
    }

    @Test
    void guardaSoloElHashSha256DelTokenDeRecuperacion() {
        Usuario usuario = usuario(7, "cliente@ejemplo.com");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(usuario);

        usuarioService.generarTokenRecuperacion(usuario.getEmail());

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarCorreoRecuperacion(eq(usuario.getEmail()), token.capture());
        assertNotEquals(token.getValue(), usuario.getResetToken());
        assertTrue(usuario.getResetToken().matches("[0-9a-f]{64}"));
        assertEquals(sha256(token.getValue()), usuario.getResetToken());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void noHaceNadaCuandoElCorreoNoExiste() {
        when(usuarioRepository.findByEmailIgnoreCase("ausente@ejemplo.com")).thenReturn(null);

        usuarioService.generarTokenRecuperacion("ausente@ejemplo.com");

        verify(emailService, never()).enviarCorreoRecuperacion(any(), any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void restablecerPasswordRevocaTodasLasSesionesActivas() {
        String token = "token-de-recuperacion";
        Usuario usuario = usuario(7, "cliente@ejemplo.com");
        usuario.setResetToken(sha256(token));
        usuario.setResetTokenExpiration(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(usuario);
        when(passwordEncoder.encode("nueva-clave")).thenReturn("bcrypt-nueva-clave");

        usuarioService.restablecerPassword(usuario.getEmail(), token, "nueva-clave");

        assertEquals("bcrypt-nueva-clave", usuario.getPassword());
        assertEquals(null, usuario.getResetToken());
        verify(sesionService).cerrarSesionesActivasPorUsuario(usuario.getId());
    }

    @Test
    void cambiarPasswordRevocaTodasLasSesionesActivas() {
        Usuario usuario = usuario(7, "cliente@ejemplo.com");
        usuario.setPassword("bcrypt-anterior");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(usuario);
        when(passwordEncoder.matches("nueva-clave", "bcrypt-anterior")).thenReturn(false);
        when(passwordEncoder.encode("nueva-clave")).thenReturn("bcrypt-nueva-clave");

        usuarioService.cambiarPassword(usuario.getEmail(), "nueva-clave");

        assertFalse("bcrypt-anterior".equals(usuario.getPassword()));
        verify(sesionService).cerrarSesionesActivasPorUsuario(usuario.getId());
    }

    @Test
    void restablecerPasswordRechazaPasswordDeMasDe20Caracteres() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.restablecerPassword(
                        "cliente@ejemplo.com", "token", "a".repeat(21)));

        assertEquals("La contraseña no puede superar los 20 caracteres.", exception.getMessage());
    }

    @Test
    void cambiarPasswordRechazaPasswordDeMasDe20Caracteres() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.cambiarPassword("cliente@ejemplo.com", "a".repeat(21)));

        assertEquals("La contraseña no puede superar los 20 caracteres.", exception.getMessage());
    }

    @Test
    void restablecerPasswordConservaLosEspaciosAlCodificar() {
        String token = "token-de-recuperacion";
        String password = " nueva-clave ";
        Usuario usuario = usuario(7, "cliente@ejemplo.com");
        usuario.setResetToken(sha256(token));
        usuario.setResetTokenExpiration(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(usuario);
        when(passwordEncoder.encode(password)).thenReturn("bcrypt-con-espacios");

        usuarioService.restablecerPassword(usuario.getEmail(), token, password);

        assertEquals("bcrypt-con-espacios", usuario.getPassword());
        verify(passwordEncoder).encode(password);
    }

    @Test
    void cambiarPasswordConservaLosEspaciosAlCodificar() {
        String password = " nueva-clave ";
        Usuario usuario = usuario(7, "cliente@ejemplo.com");
        usuario.setPassword("bcrypt-anterior");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(usuario);
        when(passwordEncoder.matches(password, "bcrypt-anterior")).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("bcrypt-con-espacios");

        usuarioService.cambiarPassword(usuario.getEmail(), password);

        assertEquals("bcrypt-con-espacios", usuario.getPassword());
        verify(passwordEncoder).encode(password);
    }

    @Test
    void cambiarPasswordAceptaExactamente20Caracteres() {
        String password = "a".repeat(20);
        Usuario usuario = usuario(7, "cliente@ejemplo.com");
        usuario.setPassword("bcrypt-anterior");
        when(usuarioRepository.findByEmailIgnoreCase(usuario.getEmail())).thenReturn(usuario);
        when(passwordEncoder.matches(password, "bcrypt-anterior")).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("bcrypt-20");

        usuarioService.cambiarPassword(usuario.getEmail(), password);

        assertEquals("bcrypt-20", usuario.getPassword());
    }

    private Usuario usuario(Integer id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        return usuario;
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
