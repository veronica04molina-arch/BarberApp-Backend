package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@ExtendWith(MockitoExtension.class)
class CitaServicioAuthorizationTest {

    @Mock private CitaServicioRepository citaServicioRepository;
    @Mock private CitaRepository citaRepository;
    @Mock private ServicioRepository servicioRepository;
    @Mock private BarberoRepository barberoRepository;
    @InjectMocks private CitaServicioService citaServicioService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void impideQueUnClienteConsulteLosServiciosDeLaCitaDeOtroCliente() {
        autenticar(cliente(1));
        CitaServicio relacion = new CitaServicio();
        relacion.setId(9);
        relacion.setIdCita(20);
        when(citaServicioRepository.findById(9)).thenReturn(Optional.of(relacion));
        when(citaRepository.findById(20)).thenReturn(Optional.of(citaDeCliente(2)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> citaServicioService.buscarPorId(9));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void permiteQueElClienteConsulteLosServiciosDeSuPropiaCita() {
        autenticar(cliente(1));
        CitaServicio relacion = new CitaServicio();
        relacion.setId(9);
        relacion.setIdCita(20);
        when(citaServicioRepository.findById(9)).thenReturn(Optional.of(relacion));
        when(citaRepository.findById(20)).thenReturn(Optional.of(citaDeCliente(1)));

        assertEquals(Optional.of(relacion), citaServicioService.buscarPorId(9));
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null));
    }

    private Usuario cliente(int id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRol("cliente");
        return usuario;
    }

    private Cita citaDeCliente(int idUsuario) {
        Cita cita = new Cita();
        cita.setId(20);
        cita.setIdUsuario(idUsuario);
        cita.setIdBarbero(7);
        return cita;
    }
}
