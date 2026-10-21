package com.barberapp.barberapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.barberapp.barberapp.auth.GoogleAuthService;
import com.barberapp.barberapp.auth.GoogleRegisterRequest;
import com.barberapp.barberapp.auth.GoogleUser;
import com.barberapp.barberapp.auth.JwtService;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.UsuarioRepository;
import com.barberapp.barberapp.service.SesionService;
import com.barberapp.barberapp.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class AuthControllerGoogleRegistrationTest {

    @Mock private UsuarioService usuarioService;
    @Mock private JwtService jwtService;
    @Mock private SesionService sesionService;
    @Mock private GoogleAuthService googleAuthService;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private AuthController authController;

    @Test
    void registroGoogleIgnoraElNombreEnviadoYUsaElDelTokenVerificado() throws Exception {
        GoogleRegisterRequest request = new GoogleRegisterRequest();
        request.setToken("token-de-google");
        request.setNombre("Nombre Manipulado");
        request.setTelefono("3001234567");
        request.setPassword("secreto");

        GoogleUser googleUser = new GoogleUser("Luis", "luis@ejemplo.com");
        when(googleAuthService.verificarToken("token-de-google")).thenReturn(googleUser);
        when(usuarioRepository.existsByEmailIgnoreCase("luis@ejemplo.com")).thenReturn(false);
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("jwt");
        when(usuarioService.guardarUsuarioConGoogle(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1);
            usuario.setRol("cliente");
            return usuario;
        });

        ResponseEntity<?> response = authController.registrarConGoogle(request);

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioService).guardarUsuarioConGoogle(usuario.capture());
        assertEquals("Luis", usuario.getValue().getNombre());
        assertEquals("luis@ejemplo.com", usuario.getValue().getEmail());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void registroGoogleRechazaPasswordDeMasDe20Caracteres() throws Exception {
        GoogleRegisterRequest request = new GoogleRegisterRequest();
        request.setToken("token-de-google");
        request.setTelefono("3001234567");
        request.setPassword("a".repeat(21));

        GoogleUser googleUser = new GoogleUser("Luis", "luis@ejemplo.com");
        when(googleAuthService.verificarToken("token-de-google")).thenReturn(googleUser);
        when(usuarioRepository.existsByEmailIgnoreCase("luis@ejemplo.com")).thenReturn(false);
        when(usuarioService.guardarUsuarioConGoogle(any(Usuario.class)))
                .thenThrow(new RuntimeException("La contraseña no puede superar los 20 caracteres."));

        ResponseEntity<?> response = authController.registrarConGoogle(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("La contraseña no puede superar los 20 caracteres.", response.getBody());
    }
}
