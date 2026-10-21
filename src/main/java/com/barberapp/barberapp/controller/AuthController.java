package com.barberapp.barberapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.auth.GoogleAuthService;
import com.barberapp.barberapp.auth.GoogleRegisterRequest;
import com.barberapp.barberapp.auth.GoogleUser;
import com.barberapp.barberapp.auth.JwtService;
import com.barberapp.barberapp.dto.LoginRequest;
import com.barberapp.barberapp.dto.LoginResponse;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.UsuarioRepository;
import com.barberapp.barberapp.service.SesionService;
import com.barberapp.barberapp.service.CorreoYaRegistradoException;
import com.barberapp.barberapp.service.UsuarioService;

@RestController
@RequestMapping("/auth")
public class AuthController {

        private final UsuarioService usuarioService;
        private final JwtService jwtService;
        private final SesionService sesionService;

        private final GoogleAuthService googleAuthService;

        private final UsuarioRepository usuarioRepository;

        public AuthController(
                        UsuarioService usuarioService,
                        JwtService jwtService,
                        SesionService sesionService,
                        GoogleAuthService googleAuthService,
                        UsuarioRepository usuarioRepository) {

                this.usuarioService = usuarioService;
                this.jwtService = jwtService;
                this.sesionService = sesionService;
                this.googleAuthService = googleAuthService;
                this.usuarioRepository = usuarioRepository;
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(
                        @RequestBody LoginRequest request) {

                try {

                        Usuario usuario = new Usuario();

                        usuario.setEmail(request.getEmail());
                        usuario.setPassword(request.getPassword());

                        Usuario usuarioAutenticado = usuarioService.iniciarSesion(usuario);

                        if (usuarioAutenticado == null) {

                                return ResponseEntity
                                                .status(401)
                                                .body("Correo o contraseña incorrectos");
                        }

                        String token = jwtService.generarToken(usuarioAutenticado);

                        sesionService.registrarSesion(
                                        usuarioAutenticado.getId(),
                                        token);

                        LoginResponse response = new LoginResponse(
                                        token,
                                        usuarioAutenticado.getId(),
                                        usuarioAutenticado.getNombre(),
                                        usuarioAutenticado.getEmail(),
                                        usuarioAutenticado.getRol());

                        return ResponseEntity.ok(response);

                } catch (RuntimeException e) {

                        // Por ejemplo: contraseña que supera la longitud máxima
                        // permitida. Se rechaza sin truncar ni modificar el valor.
                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }

        // verificar los datos de Google
        @PostMapping("/google/verificar")
        public ResponseEntity<?> verificarGoogle(
                        @RequestBody GoogleRegisterRequest request) {

                try {

                        GoogleUser googleUser = googleAuthService.verificarToken(request.getToken());

                        if (googleUser == null) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Token de Google inválido.");
                        }

                        if (usuarioRepository.existsByEmailIgnoreCase(
                                        googleUser.getEmail())) {

                                return ResponseEntity
                                                .status(409)
                                                .body("Ya existe una cuenta registrada con ese correo.");
                        }

                        return ResponseEntity.ok(googleUser);

                } catch (Exception e) {

                        return ResponseEntity
                                        .internalServerError()
                                        .body("No fue posible verificar la cuenta de Google.");
                }
        }

        // completar registro y crear usuario
        @PostMapping("/google/registrar")
        public ResponseEntity<?> registrarConGoogle(
                        @RequestBody GoogleRegisterRequest request) {

                try {

                        GoogleUser googleUser = googleAuthService.verificarToken(request.getToken());

                        if (googleUser == null) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Token de Google inválido.");
                        }

                        if (usuarioRepository.existsByEmailIgnoreCase(
                                        googleUser.getEmail())) {

                                return ResponseEntity
                                                .status(409)
                                                .body("Ya existe una cuenta registrada con ese correo.");
                        }

                        // El nombre siempre proviene del token de Google que acabamos de verificar.
                        String nombre = googleUser.getNombre();

                        Usuario nuevoUsuario = new Usuario();

                        nuevoUsuario.setNombre(nombre);
                        nuevoUsuario.setEmail(googleUser.getEmail());
                        nuevoUsuario.setTelefono(request.getTelefono());
                        nuevoUsuario.setPassword(request.getPassword());

                        Usuario usuarioRegistrado = usuarioService.guardarUsuarioConGoogle(nuevoUsuario);

                        // Generar JWT de BarberApp
                        String token = jwtService.generarToken(usuarioRegistrado);

                        // Registrar sesión
                        sesionService.registrarSesion(
                                        usuarioRegistrado.getId(),
                                        token);

                        LoginResponse response = new LoginResponse(
                                        token,
                                        usuarioRegistrado.getId(),
                                        usuarioRegistrado.getNombre(),
                                        usuarioRegistrado.getEmail(),
                                        usuarioRegistrado.getRol());

                        return ResponseEntity.ok(response);

                } catch (CorreoYaRegistradoException e) {

                        return ResponseEntity
                                        .status(409)
                                        .body("Ya existe una cuenta registrada con ese correo.");

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());

                } catch (Exception e) {

                        return ResponseEntity
                                        .internalServerError()
                                        .body("No fue posible completar el registro con Google.");
                }
        }

        @PostMapping("/logout")
        public ResponseEntity<?> logout(
                        jakarta.servlet.http.HttpServletRequest request) {

                String authorizationHeader = request.getHeader("Authorization");

                if (authorizationHeader == null
                                || !authorizationHeader.startsWith("Bearer ")) {

                        return ResponseEntity
                                        .status(401)
                                        .body("No se encontró el token de autenticación.");
                }

                String token = authorizationHeader.substring(7);

                try {

                        sesionService.cerrarSesionPorToken(token);

                        return ResponseEntity.ok(
                                        "Sesión cerrada correctamente.");

                } catch (ResponseStatusException e) {

                        return ResponseEntity
                                        .status(e.getStatusCode())
                                        .body(e.getReason());
                }
        }
}
