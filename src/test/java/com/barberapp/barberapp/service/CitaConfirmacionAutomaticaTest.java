package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CitaConfirmacionAutomaticaTest {

    private static final Integer ID_CITA = 15;
    private static final Integer ID_CLIENTE = 8;
    private static final Integer ID_BARBERO = 3;

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BarberoRepository barberoRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CitaServicioRepository citaServicioRepository;

    @Mock
    private DisponibilidadService disponibilidadService;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private CitaService citaService;

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pendienteCreadaHaceDiezMinutosSiguePendienteYSePuedeModificar() {
        LocalDateTime ahora = LocalDateTime.now();
        Cita cita = cita("pendiente", ahora.minusMinutes(10));
        Cita cambios = cambiosDe(cita);

        when(citaRepository.findByEstadoAndFechaCreacionLessThanEqual(
                "pendiente", ahora.minusMinutes(30))).thenReturn(List.of());
        citaService.confirmarCitasPendientesVencidas(ahora);

        prepararActualizacionComoCliente(cita);

        assertDoesNotThrow(() -> citaService.actualizarCita(ID_CITA, cambios));
        assertEquals("pendiente", cita.getEstado());
        assertEquals("Nota actualizada", cita.getNotas());
    }

    @Test
    void pendienteCreadaHaceExactamenteTreintaMinutosSeConfirmaYNoSePuedeModificar() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 14, 30);
        Cita cita = cita("pendiente", ahora.minusMinutes(30));

        confirmarPendientes(ahora, cita);

        assertEquals("confirmada", cita.getEstado());
        verify(citaRepository).save(cita);
        verificarModificacionRechazada(cita);
    }

    @Test
    void pendienteCreadaHaceTreintaYUnMinutosSeConfirmaYNoSePuedeModificar() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 14, 31);
        Cita cita = cita("pendiente", ahora.minusMinutes(31));

        confirmarPendientes(ahora, cita);

        assertEquals("confirmada", cita.getEstado());
        verificarModificacionRechazada(cita);
    }

    @Test
    void citaConfirmadaNoVuelveAPendiente() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 14, 30);
        Cita cita = cita("confirmada", ahora.minusHours(2));

        confirmarPendientes(ahora, cita);

        assertEquals("confirmada", cita.getEstado());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void citaCanceladaPermaneceCancelada() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 14, 30);
        Cita cita = cita("cancelada", ahora.minusHours(2));

        confirmarPendientes(ahora, cita);

        assertEquals("cancelada", cita.getEstado());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void citaCompletadaPermaneceCompletada() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 14, 30);
        Cita cita = cita("completada", ahora.minusHours(2));

        confirmarPendientes(ahora, cita);

        assertEquals("completada", cita.getEstado());
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void citaFuturaCreadaHaceMasDeTreintaMinutosSeConfirma() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 14, 30);
        Cita cita = cita("pendiente", ahora.minusMinutes(31));
        cita.setFecha(LocalDate.of(2026, 10, 8));
        cita.setHora(LocalTime.of(10, 0));

        confirmarPendientes(ahora, cita);

        assertEquals("confirmada", cita.getEstado());
    }

    private void confirmarPendientes(LocalDateTime ahora, Cita cita) {
        when(citaRepository.findByEstadoAndFechaCreacionLessThanEqual(
                eq("pendiente"), eq(ahora.minusMinutes(30))))
                .thenReturn(List.of(cita));

        citaService.confirmarCitasPendientesVencidas(ahora);
    }

    private void verificarModificacionRechazada(Cita cita) {
        prepararAccesoComoCliente(cita);

        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
                () -> citaService.actualizarCita(ID_CITA, cambiosDe(cita)));

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatusCode());
        assertEquals("La cita ya fue confirmada y no puede modificarse.", excepcion.getReason());
    }

    private void prepararActualizacionComoCliente(Cita cita) {
        prepararAccesoComoCliente(cita);
        Usuario cliente = cliente();
        when(usuarioRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(barberoRepository.existsById(ID_BARBERO)).thenReturn(true);
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void prepararAccesoComoCliente(Cita cita) {
        Usuario cliente = cliente();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cliente, null));
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));
    }

    private Cita cita(String estado, LocalDateTime fechaCreacion) {
        Cita cita = new Cita();
        cita.setId(ID_CITA);
        cita.setIdUsuario(ID_CLIENTE);
        cita.setIdBarbero(ID_BARBERO);
        cita.setFecha(LocalDate.now().plusDays(3));
        cita.setHora(LocalTime.of(10, 0));
        cita.setEstado(estado);
        cita.setFechaCreacion(fechaCreacion);
        cita.setNotas("Nota original");
        return cita;
    }

    private Cita cambiosDe(Cita cita) {
        Cita cambios = new Cita();
        cambios.setIdUsuario(cita.getIdUsuario());
        cambios.setIdBarbero(cita.getIdBarbero());
        cambios.setFecha(cita.getFecha());
        cambios.setHora(cita.getHora());
        cambios.setNotas("Nota actualizada");
        return cambios;
    }

    private Usuario cliente() {
        Usuario cliente = new Usuario();
        cliente.setId(ID_CLIENTE);
        cliente.setRol("cliente");
        return cliente;
    }
}
