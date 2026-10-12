package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CitaServiceActualizarTest {

    private static final int ID_CITA = 31;
    private static final int ID_USUARIO = 4;
    private static final int ID_BARBERO = 7;

    @Mock private CitaRepository citaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private BarberoRepository barberoRepository;
    @Mock private ServicioRepository servicioRepository;
    @Mock private CitaServicioRepository citaServicioRepository;
    @Mock private DisponibilidadService disponibilidadService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks private CitaService citaService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void actualizaLaMismaCitaSinCrearRelacionesDeServicio() {
        Cita existente = cita(LocalDate.now().plusDays(2), LocalTime.of(17, 30));
        Cita cambios = cita(LocalDate.now().plusDays(3), LocalTime.of(10, 0));
        prepararUsuarioAutenticado();
        prepararDuracionCita();
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuario(ID_USUARIO, "cliente")));
        when(barberoRepository.existsById(ID_BARBERO)).thenReturn(true);
        when(disponibilidadService.estaDisponible(
                eq(ID_BARBERO), eq(cambios.getFecha()), eq(cambios.getHora()), eq(30), eq(ID_CITA)))
                .thenReturn(true);
        when(citaRepository.save(existente)).thenReturn(existente);
        Barbero barbero = new Barbero();
        barbero.setUsuario(usuario(8, "barbero"));
        when(barberoRepository.findById(ID_BARBERO)).thenReturn(Optional.of(barbero));

        Cita resultado = citaService.actualizarCita(ID_CITA, cambios);

        assertEquals(ID_CITA, resultado.getId());
        assertEquals(cambios.getFecha(), existente.getFecha());
        assertEquals(cambios.getHora(), existente.getHora());
        verify(citaRepository).save(existente);
        verify(citaServicioRepository, never()).save(any(CitaServicio.class));
    }

    @Test
    void rechazaElNuevoHorarioOcupadoSinPersistirCambios() {
        Cita existente = cita(LocalDate.now().plusDays(2), LocalTime.of(17, 30));
        Cita cambios = cita(LocalDate.now().plusDays(3), LocalTime.of(10, 0));
        prepararUsuarioAutenticado();
        prepararDuracionCita();
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuario(ID_USUARIO, "cliente")));
        when(barberoRepository.existsById(ID_BARBERO)).thenReturn(true);
        when(disponibilidadService.estaDisponible(
                eq(ID_BARBERO), eq(cambios.getFecha()), eq(cambios.getHora()), eq(30), eq(ID_CITA)))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () -> citaService.actualizarCita(ID_CITA, cambios));

        verify(citaRepository, never()).save(any(Cita.class));
        verify(citaServicioRepository, never()).save(any(CitaServicio.class));
    }

    private void prepararUsuarioAutenticado() {
        Usuario usuario = usuario(ID_USUARIO, "cliente");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null));
    }

    private void prepararDuracionCita() {
        CitaServicio relacion = new CitaServicio();
        relacion.setIdCita(ID_CITA);
        relacion.setIdServicio(11);
        Servicio servicio = new Servicio();
        servicio.setId(11);
        servicio.setDuracion(30);
        when(citaServicioRepository.findByIdCita(ID_CITA)).thenReturn(List.of(relacion));
        when(servicioRepository.findById(11)).thenReturn(Optional.of(servicio));
    }

    private Cita cita(LocalDate fecha, LocalTime hora) {
        Cita cita = new Cita();
        cita.setId(ID_CITA);
        cita.setIdUsuario(ID_USUARIO);
        cita.setIdBarbero(ID_BARBERO);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado("pendiente");
        cita.setFechaCreacion(LocalDateTime.now().minusMinutes(5));
        cita.setNotas("Nota");
        return cita;
    }

    private Usuario usuario(int id, String rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRol(rol);
        return usuario;
    }
}
