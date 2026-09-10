package com.barberapp.barberapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.auth.GoogleAuthService;
import com.barberapp.barberapp.auth.GoogleRegisterRequest;
import com.barberapp.barberapp.auth.GoogleUser;
import com.barberapp.barberapp.auth.JwtService;
import com.barberapp.barberapp.dto.LoginRequest;
import com.barberapp.barberapp.dto.LoginResponse;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.UsuarioRepository;
import com.barberapp.barberapp.service.UsuarioService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

        private final UsuarioService usuarioService;
        private final JwtService jwtService;

        @Autowired
        private GoogleAuthService googleAuthService;

        @Autowired
        private UsuarioRepository usuarioRepository;

        public AuthController(
                        UsuarioService usuarioService,
                        JwtService jwtService) {

                this.usuarioService = usuarioService;
                this.jwtService = jwtService;
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(
                        @RequestBody LoginRequest request) {

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

                LoginResponse response = new LoginResponse(
                                token,
                                usuarioAutenticado.getId(),
                                usuarioAutenticado.getNombre(),
                                usuarioAutenticado.getEmail(),
                                usuarioAutenticado.getRol());

                return ResponseEntity.ok(response);
        }

        @PostMapping
        public ResponseEntity<?> verificarGoogle(
                        @RequestBody GoogleRegisterRequest request) {

                try {

                        GoogleUser googleUser = googleAuthService.verificarToken(request.getToken());

                        if (googleUser == null) {

                                return ResponseEntity.badRequest()
                                                .body("Token de Google inválido.");

                        }

                        if (usuarioRepository.existsByEmailIgnoreCase(googleUser.getEmail())) {

                                return ResponseEntity.status(409)
                                                .body("Ya existe una cuenta registrada con ese correo.");

                        }

                        return ResponseEntity.ok(googleUser);

                } catch (Exception e) {

                        return ResponseEntity.internalServerError()
                                        .body(e.getMessage());

                }

        }

}
