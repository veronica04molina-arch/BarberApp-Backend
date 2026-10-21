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

    @Test
    void registroConservaLosEspaciosAlCodificarLaPassword() {
        Usuario usuario = usuario("Luis Fernando");
        String password = " secreta ";
        usuario.setPassword(password);
        when(usuarioRepository.existsByEmailIgnoreCase("luis@ejemplo.com")).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("bcrypt-con-espacios");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario registrado = usuarioService.guardarUsuario(usuario);

        assertEquals("bcrypt-con-espacios", registrado.getPassword());
        verify(passwordEncoder).encode(password);
    }

    @Test
    void registroNormalRechazaPasswordDeMasDe20Caracteres() {
        Usuario usuario = usuario("Luis Fernando");
        usuario.setPassword("a".repeat(21));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.guardarUsuario(usuario));

        assertEquals("La contraseña no puede superar los 20 caracteres.", exception.getMessage());
    }

    @Test
    void registroGoogleRechazaPasswordDeMasDe20Caracteres() {
        Usuario usuario = usuario("luis");
        usuario.setPassword("a".repeat(21));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.guardarUsuarioConGoogle(usuario));

        assertEquals("La contraseña no puede superar los 20 caracteres.", exception.getMessage());
    }

    @Test
    void loginRechazaPasswordDeMasDe20Caracteres() {
        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@ejemplo.com");
        usuario.setPassword("a".repeat(21));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> usuarioService.iniciarSesion(usuario));

        assertEquals("La contraseña no puede superar los 20 caracteres.", exception.getMessage());
    }

    @Test
    void aceptaPasswordDeExactamente20CaracteresEnRegistroGoogle() {
        Usuario usuario = usuario("luis");
        String passwordDe20 = "a".repeat(20);
        usuario.setPassword(passwordDe20);
        when(usuarioRepository.existsByEmailIgnoreCase("luis@ejemplo.com")).thenReturn(false);
        when(passwordEncoder.encode(passwordDe20)).thenReturn("bcrypt-20");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario registrado = usuarioService.guardarUsuarioConGoogle(usuario);

        assertEquals("bcrypt-20", registrado.getPassword());
    }

    @Test
    void loginConservaLosEspaciosAlCompararLaPassword() {
        String password = " secreta ";
        Usuario solicitud = new Usuario();
        solicitud.setEmail("cliente@ejemplo.com");
        solicitud.setPassword(password);
        Usuario usuarioEncontrado = new Usuario();
        usuarioEncontrado.setPassword("bcrypt-con-espacios");
        when(usuarioRepository.findByEmailIgnoreCase("cliente@ejemplo.com")).thenReturn(usuarioEncontrado);
        when(passwordEncoder.matches(password, "bcrypt-con-espacios")).thenReturn(true);

        assertEquals(usuarioEncontrado, usuarioService.iniciarSesion(solicitud));
        verify(passwordEncoder).matches(password, "bcrypt-con-espacios");
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
