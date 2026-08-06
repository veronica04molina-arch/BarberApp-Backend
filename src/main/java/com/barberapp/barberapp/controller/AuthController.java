package com.barberapp.barberapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.barberapp.barberapp.auth.GoogleAuthService;
import com.barberapp.barberapp.auth.GoogleRegisterRequest;
import com.barberapp.barberapp.auth.GoogleUser;
import com.barberapp.barberapp.repository.UsuarioRepository;

@RestController
@RequestMapping("/auth/google")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<?> verificarGoogle(
            @RequestBody GoogleRegisterRequest request) {

        try {

            GoogleUser googleUser =
                    googleAuthService.verificarToken(request.getToken());

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
