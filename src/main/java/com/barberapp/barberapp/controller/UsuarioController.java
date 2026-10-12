package com.barberapp.barberapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.dto.CambiarPasswordDTO;
import com.barberapp.barberapp.dto.RestablecerPasswordDTO;
import com.barberapp.barberapp.dto.SolicitarRecuperacionDTO;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.service.CorreoYaRegistradoException;
import com.barberapp.barberapp.service.UsuarioService;
import org.springframework.security.core.Authentication;

@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {

        try {

            return ResponseEntity.ok(
                    usuarioService.guardarUsuario(usuario));

        } catch (CorreoYaRegistradoException e) {

            return ResponseEntity
                    .status(409)
                    .body(e.getMessage());

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public Usuario buscarUsuarioPorId(@PathVariable Integer id) {
        return usuarioService.buscarUsuarioPorId(id);
    }

    @PutMapping("/{id}")
    public Usuario actualizarUsuario(
            @PathVariable Integer id,
            @RequestBody Usuario usuario) {

        return usuarioService.actualizarTelefono(id, usuario.getTelefono());
    }

    // ==========================================
    // ENDPOINTS DE RECUPERACIÓN DE CONTRASEÑA
    // ==========================================

    @PostMapping("/olvide-password")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody SolicitarRecuperacionDTO dto) {
        try {
            usuarioService.generarTokenRecuperacion(dto.getEmail());
            return ResponseEntity.ok(Map.of("message", "Instrucciones enviadas con éxito."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<?> restablecerPassword(@RequestBody RestablecerPasswordDTO dto) {
        try {
            usuarioService.restablecerPassword(dto.getEmail(), dto.getToken(), dto.getNuevaPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> cambiarPassword(
            @RequestBody CambiarPasswordDTO dto,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "No autenticado."));
        }

        if (!(authentication.getPrincipal() instanceof Usuario usuario)) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "No autenticado."));
        }

        try {
            usuarioService.cambiarPassword(usuario.getEmail(), dto.getPassword());

            return ResponseEntity.ok(
                    Map.of("message", "Contraseña actualizada exitosamente."));

        } catch (RuntimeException e) {
            String mensaje = e.getMessage() != null
                    ? e.getMessage()
                    : "No se pudo actualizar la contraseña.";

            return ResponseEntity.badRequest()
                    .body(Map.of("message", mensaje));
        }
    }
}