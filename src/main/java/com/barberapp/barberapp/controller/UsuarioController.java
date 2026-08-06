package com.barberapp.barberapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.service.CorreoYaRegistradoException;
import com.barberapp.barberapp.service.UsuarioService;

@CrossOrigin(origins = "http://127.0.0.1:5500")
/**
 * Controlador encargado de gestionar las peticiones HTTP relacionadas con los usuarios.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    /**
 * Registra un nuevo usuario.
 */
    @PostMapping
public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {

    try {

        return ResponseEntity.ok(
                usuarioService.guardarUsuario(usuario)
        );

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
    /**
 * Obtiene la lista de usuarios registrados.
 */
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }
    /**
 * Busca un usuario por su identificador.
 */
    @GetMapping("/{id}")
    public Usuario buscarUsuarioPorId(@PathVariable Integer id) {
        return usuarioService.buscarUsuarioPorId(id);
    }
    /**
 * Actualiza la información de un usuario.
 */
    @PutMapping("/{id}")
    public Usuario actualizarUsuario(@PathVariable Integer id,
                                @RequestBody Usuario usuario) {
        return usuarioService.actualizarUsuario(id, usuario);
    }
    /**
 * Elimina un usuario por su identificador.
 */
    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
    }
    @PostMapping("/login")
    public Usuario iniciarSesion(@RequestBody Usuario usuario) {
        return usuarioService.iniciarSesion(usuario);
    }
}
