package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Normaliza el nombre del usuario.
    
    private String normalizarNombre(String nombre) {

        return Arrays.stream(
                nombre.trim()
                        .toLowerCase()
                        .split("\\s+"))
                .map(palabra -> palabra.substring(0, 1).toUpperCase()
                        + palabra.substring(1))
                .collect(Collectors.joining(" "));
    }

    public Usuario guardarUsuario(Usuario usuario) {

        // Validar que los datos del usuario existan.
        if (usuario == null) {
            throw new RuntimeException(
                    "Los datos del usuario son obligatorios.");
        }

        // Validar nombre.
        if (usuario.getNombre() == null
                || usuario.getNombre().trim().isEmpty()) {

            throw new RuntimeException(
                    "El nombre es obligatorio.");
        }

        // Limpiar y normalizar el nombre.
        String nombre = normalizarNombre(usuario.getNombre());

        // nombre + al menos un apellido.
        String[] partesNombre = nombre.split("\\s+");

        if (partesNombre.length < 2) {
            throw new RuntimeException(
                    "Debes ingresar tu nombre y al menos un apellido.");
        }

        // Validar que el nombre contenga solo letras y espacios.
        if (!nombre.matches("[\\p{L} ]+")) {
            throw new RuntimeException(
                    "El nombre solo puede contener letras y espacios.");
        }

        // Validar email.
        if (usuario.getEmail() == null
                || usuario.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "El correo electrónico es obligatorio.");
        }

        String email = usuario.getEmail().trim().toLowerCase();

        // Validar formato del email.
        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new RuntimeException(
                    "El correo electrónico no tiene un formato válido.");
        }

        // Validar que el correo no esté registrado.
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new CorreoYaRegistradoException();
        }

        // Validar teléfono.
        if (usuario.getTelefono() == null
                || !usuario.getTelefono().trim().matches("\\d{10}")) {

            throw new RuntimeException(
                    "El teléfono debe tener exactamente 10 dígitos.");
        }

        // Validar contraseña.
        if (usuario.getPassword() == null
                || usuario.getPassword().length() < 6) {

            throw new RuntimeException(
                    "La contraseña debe tener al menos 6 caracteres.");
        }

        // Normalizar datos.
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setTelefono(usuario.getTelefono().trim());

        // Todo registro público se crea como cliente.
        usuario.setRol("cliente");

        // La fecha se genera automáticamente en el backend.
        usuario.setFechaRegistro(LocalDate.now());

        // La contraseña se guarda como hash BCrypt.
        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    /**
     * Actualiza únicamente el teléfono del usuario.
     */
    public Usuario actualizarTelefono(
            Integer id,
            String telefono) {

        Usuario usuarioExistente = usuarioRepository.findById(id).orElse(null);

        if (usuarioExistente == null) {
            return null;
        }

        if (telefono == null
                || !telefono.trim().matches("\\d{10}")) {

            throw new RuntimeException(
                    "El teléfono debe tener exactamente 10 dígitos.");
        }

        usuarioExistente.setTelefono(telefono.trim());

        return usuarioRepository.save(usuarioExistente);
    }

    /**
     * Valida las credenciales para iniciar sesión.
     */
    public Usuario iniciarSesion(Usuario usuario) {

        if (usuario == null
                || usuario.getEmail() == null
                || usuario.getPassword() == null) {

            return null;
        }

        Usuario usuarioEncontrado = usuarioRepository.findByEmailIgnoreCase(
                usuario.getEmail().trim());

        if (usuarioEncontrado == null
                || !passwordEncoder.matches(
                        usuario.getPassword(),
                        usuarioEncontrado.getPassword())) {

            return null;
        }

        return usuarioEncontrado;
    }
}
