package com.barberapp.barberapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.barberapp.barberapp.auth.GoogleAuthService;
import com.barberapp.barberapp.auth.JwtService;
import com.barberapp.barberapp.dto.LoginRequest;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.UsuarioRepository;
import com.barberapp.barberapp.service.SesionService;
import com.barberapp.barberapp.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class AuthControllerLoginTest {

    @Mock private UsuarioService usuarioService;
    @Mock private JwtService jwtService;
    @Mock private SesionService sesionService;
    @Mock private GoogleAuthService googleAuthService;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private AuthController authController;

    @Test
    void loginRechazaPasswordDeMasDe20CaracteresSinTruncarla() {
        LoginRequest request = new LoginRequest();
        request.setEmail("cliente@ejemplo.com");
        request.setPassword("a".repeat(21));

        when(usuarioService.iniciarSesion(org.mockito.ArgumentMatchers.any(Usuario.class)))
                .thenThrow(new RuntimeException("La contraseña no puede superar los 20 caracteres."));

        ResponseEntity<?> response = authController.login(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("La contraseña no puede superar los 20 caracteres.", response.getBody());
    }

    @Test
    void loginConservaRespuesta401ParaCredencialesIncorrectas() {
        LoginRequest request = new LoginRequest();
        request.setEmail("cliente@ejemplo.com");
        request.setPassword("incorrecta");

        when(usuarioService.iniciarSesion(org.mockito.ArgumentMatchers.any(Usuario.class)))
                .thenReturn(null);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(401, response.getStatusCode().value());
    }
}