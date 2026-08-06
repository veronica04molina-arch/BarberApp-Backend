package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.UsuarioRepository;

/**
 * Contiene la lógica de negocio relacionada con los usuarios.
 * Se comunica entre el controlador y el repositorio.
 */
@Service
public class UsuarioService {

private final UsuarioRepository usuarioRepository;
private final PasswordEncoder passwordEncoder;

public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
}
/**
 * Guarda un nuevo usuario en la base de datos.
 */
public Usuario guardarUsuario(Usuario usuario) {

    if (usuario == null || usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()
            || usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()
            || usuario.getTelefono() == null || !usuario.getTelefono().matches("\\d{10}")
            || usuario.getPassword() == null || usuario.getPassword().length() < 6) {
        throw new RuntimeException("Datos de registro inválidos. La contraseña debe tener al menos 6 caracteres.");
    }

    usuario.setEmail(usuario.getEmail().trim().toLowerCase());
    usuario.setNombre(usuario.getNombre().trim());
    usuario.setTelefono(usuario.getTelefono().trim());
    usuario.setRol("cliente");

    if (usuarioRepository.existsByEmailIgnoreCase(usuario.getEmail())) {

        throw new CorreoYaRegistradoException();

    }

    usuario.setFechaRegistro(LocalDate.now());
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

    return usuarioRepository.save(usuario);

}
/**
 * Obtiene la lista de todos los usuarios registrados.
 */
public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
/**
 * Busca un usuario por su ID.
 */
public Usuario buscarUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
}
/**
 * Actualiza la información de un usuario existente.
 */
public Usuario actualizarUsuario(Integer id, Usuario usuarioActualizado) {
        Usuario usuarioExistente = usuarioRepository.findById(id).orElse(null);
        if (usuarioExistente != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isBlank()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
            }
            usuarioExistente.setRol(usuarioActualizado.getRol());
            usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
            return usuarioRepository.save(usuarioExistente);
        }
        return null;
    }
/**
 * Elimina un usuario por su ID.
 */
public void eliminarUsuario(Integer id) {
    usuarioRepository.deleteById(id);
    }
/**
 * Valida las credenciales del usuario para iniciar sesión.
 */
public Usuario iniciarSesion(Usuario usuario) {

    if (usuario == null || usuario.getEmail() == null || usuario.getPassword() == null) {
        return null;
    }

    Usuario usuarioEncontrado = usuarioRepository.findByEmailIgnoreCase(usuario.getEmail().trim());

    if (usuarioEncontrado == null || !passwordEncoder.matches(usuario.getPassword(), usuarioEncontrado.getPassword())) {
        return null;
    }

    return usuarioEncontrado;

}
}
