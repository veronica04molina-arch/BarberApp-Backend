package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceRegistrationTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private BarberoRepository barberoRepository;
    @Mock private CitaRepository citaRepository;
    @Mock private SesionService sesionService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, emailService,
                barberoRepository, citaRepository, sesionService);
    }

    @Test
    void registroNormalConUnaSolaPalabraSigueFallando() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.guardarUsuario(usuario("Luis")));

        assertEquals("Debes ingresar tu nombre y al menos un apellido.", exception.getMessage());
    }

    @Test
    void registroGoogleConUnaSolaPalabraConservaLasDemásValidaciones() {
        Usuario usuario = usuario("luis");
        when(usuarioRepository.existsByEmailIgnoreCase("luis@ejemplo.com")).thenReturn(false);
        when(passwordEncoder.encode("secreto")).thenReturn("bcrypt-secreto");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario registrado = usuarioService.guardarUsuarioConGoogle(usuario);

        assertEquals("Luis", registrado.getNombre());
        assertEquals("luis@ejemplo.com", registrado.getEmail());
        assertEquals("bcrypt-secreto", registrado.getPassword());
        assertEquals("cliente", registrado.getRol());
        verify(usuarioRepository).save(usuario);
    }

    private Usuario usuario(String nombre) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail("luis@ejemplo.com");
        usuario.setTelefono("3001234567");
        usuario.setPassword("secreto");
        return usuario;
    }
}
