package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final BarberoRepository barberoRepository;
    private final CitaRepository citaRepository;
    private final SesionService sesionService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            BarberoRepository barberoRepository,
            CitaRepository citaRepository,
            SesionService sesionService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.barberoRepository = barberoRepository;
        this.citaRepository = citaRepository;
        this.sesionService = sesionService;
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

        return guardarUsuario(usuario, true);
    }

    /**
     * Guarda un usuario creado desde una identidad de Google ya verificada.
     * Conserva las mismas validaciones del registro público, excepto que el
     * nombre puede estar compuesto por una sola palabra.
     */
    public Usuario guardarUsuarioConGoogle(Usuario usuario) {

        return guardarUsuario(usuario, false);
    }

    private Usuario guardarUsuario(Usuario usuario, boolean exigirApellido) {

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

        if (exigirApellido && partesNombre.length < 2) {
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
        return List.of(usuarioAutenticado());
    }

    public Usuario buscarUsuarioPorId(Integer id) {
        autorizarLecturaUsuario(id);
        return usuarioRepository.findById(id).orElse(null);
    }

    /**
     * Actualiza únicamente el teléfono del usuario.
     */
    public Usuario actualizarTelefono(
            Integer id,
            String telefono) {

        autorizarUsuario(id);

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

    /**
     * Genera un token de recuperación para el usuario y establece una expiración de
     * 15 minutos.
     */
    public void generarTokenRecuperacion(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim());

        if (usuario == null) {
            return;
        }

        // Generar token
        String token = UUID.randomUUID().toString();
        usuario.setResetToken(hashTokenRecuperacion(token));
        usuario.setResetTokenExpiration(LocalDateTime.now().plusMinutes(15));

        usuarioRepository.save(usuario);

        // 2. Enviar el correo electrónico
        emailService.enviarCorreoRecuperacion(usuario.getEmail(), token);

    }

    /**
     * Valida el token/correo y actualiza la contraseña en la base de datos con
     * BCrypt.
     */
    @Transactional
    public void restablecerPassword(String email, String token, String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        if (email == null || token == null || token.isBlank()) {
            throw new RuntimeException("El código o token de recuperación es inválido.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim());

        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado.");
        }

        // Validar token y tiempo de expiración
        if (usuario.getResetToken() == null || !MessageDigest.isEqual(
                usuario.getResetToken().getBytes(StandardCharsets.UTF_8),
                hashTokenRecuperacion(token).getBytes(StandardCharsets.UTF_8))) {
            throw new RuntimeException("El código o token de recuperación es inválido.");
        }

        if (usuario.getResetTokenExpiration() == null
                || usuario.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token de recuperación ha expirado. Solicita uno nuevo.");
        }

        // Hashear y actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(nuevaPassword.trim()));

        // Limpiar el token para que no se reutlice
        usuario.setResetToken(null);
        usuario.setResetTokenExpiration(null);

        usuarioRepository.save(usuario);
        sesionService.cerrarSesionesActivasPorUsuario(usuario.getId());
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     */
    @Transactional
    public void cambiarPassword(String identificador, String nuevaPassword) {

    if (nuevaPassword == null || nuevaPassword.trim().length() < 8) {
        throw new RuntimeException(
                "La nueva contraseña debe tener al menos 8 caracteres.");
    }

    Usuario usuario = buscarPorIdentificador(identificador);

    if (usuario == null) {
        throw new RuntimeException("Usuario no encontrado.");
    }

    if (passwordEncoder.matches(nuevaPassword, usuario.getPassword())) {
        throw new RuntimeException(
                "La nueva contraseña no puede ser igual a la actual.");
    }

    usuario.setPassword(passwordEncoder.encode(nuevaPassword.trim()));
    usuario.setResetToken(null);
    usuario.setResetTokenExpiration(null);

    usuarioRepository.save(usuario);
    sesionService.cerrarSesionesActivasPorUsuario(usuario.getId());
}

private String hashTokenRecuperacion(String token) {
    try {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("No está disponible SHA-256.", e);
    }
}

private Usuario buscarPorIdentificador(String identificador) {

    if (identificador == null) {
        return null;
    }

    String valor = identificador.trim();

    Usuario porEmail = usuarioRepository.findByEmailIgnoreCase(valor);

    if (porEmail != null) {
        return porEmail;
    }

    try {
        return usuarioRepository.findById(Integer.parseInt(valor)).orElse(null);
    } catch (NumberFormatException e) {
        return null;
    }
}

    private Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay un usuario autenticado.");
        }
        return usuario;
    }

    private void autorizarUsuario(Integer idUsuario) {
        if (idUsuario == null || !usuarioAutenticado().getId().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para acceder a este usuario.");
        }
    }

    private void autorizarLecturaUsuario(Integer idUsuario) {
        Usuario usuarioAutenticado = usuarioAutenticado();

        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para acceder a este usuario.");
        }

        if ("cliente".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            autorizarUsuario(idUsuario);
            return;
        }

        if ("barbero".equalsIgnoreCase(usuarioAutenticado.getRol())) {
            boolean tieneCitaConElCliente = barberoRepository.findByUsuarioId(usuarioAutenticado.getId())
                    .map(barbero -> citaRepository.existsByIdUsuarioAndIdBarbero(idUsuario, barbero.getId()))
                    .orElse(false);

            if (tieneCitaConElCliente) {
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No tienes permiso para acceder a este usuario.");
    }
}
