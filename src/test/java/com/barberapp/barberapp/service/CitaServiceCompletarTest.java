package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CitaServiceCompletarTest {

    private static final int ID_CITA = 12;
    private static final int ID_BARBERO = 7;
    private static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");

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
    void rechazaUnaCitaPendienteAunqueSuHoraYaHayaLlegado() {
        Cita cita = cita("pendiente", LocalDateTime.now(ZONA_BOGOTA).minusMinutes(1));
        cita.setFechaCreacion(LocalDateTime.now(ZONA_BOGOTA).minusMinutes(5));
        prepararBarberoAsignado(20, ID_BARBERO);
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> citaService.completarCita(ID_CITA));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("No se puede completar una cita pendiente. Debe estar confirmada.", error.getReason());
        verify(citaRepository, never()).save(cita);
    }

    @Test
    void rechazaUnaCitaConfirmadaCuyaDuracionAunNoTermina() {
        Cita cita = cita("confirmada", LocalDateTime.now(ZONA_BOGOTA).minusMinutes(1));
        prepararBarberoAsignado(20, ID_BARBERO);
        prepararDuracionCita(30);
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> citaService.completarCita(ID_CITA));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("No se puede completar la cita hasta que haya transcurrido la duración programada del servicio.", error.getReason());
        verify(citaRepository, never()).save(cita);
    }

    @Test
    void completaUnaCitaConfirmadaCuyaDuracionYaTermino() {
        Cita cita = cita("confirmada", LocalDateTime.now(ZONA_BOGOTA).minusMinutes(31));
        prepararBarberoAsignado(20, ID_BARBERO);
        prepararDuracionCita(30);
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));

        assertEquals(true, citaService.completarCita(ID_CITA));
        assertEquals("completada", cita.getEstado());
        verify(citaRepository).save(cita);
    }

    @Test
    void rechazaUnaCitaCancelada() {
        Cita cita = cita("cancelada", LocalDateTime.now(ZONA_BOGOTA).minusMinutes(1));
        prepararBarberoAsignado(20, ID_BARBERO);
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> citaService.completarCita(ID_CITA));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(citaRepository, never()).save(cita);
    }

    @Test
    void rechazaUnaCitaQueYaFueCompletada() {
        Cita cita = cita("completada", LocalDateTime.now(ZONA_BOGOTA).minusMinutes(1));
        prepararBarberoAsignado(20, ID_BARBERO);
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> citaService.completarCita(ID_CITA));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(citaRepository, never()).save(cita);
    }

    @Test
    void rechazaAUnClienteAunqueLaCitaPodriaCompletarse() {
        Cita cita = cita("confirmada", LocalDateTime.now(ZONA_BOGOTA).minusMinutes(31));
        autenticar(usuario(4, "cliente"));
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> citaService.completarCita(ID_CITA));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(citaRepository, never()).save(cita);
    }

    @Test
    void rechazaAlBarberoNoAsignado() {
        Cita cita = cita("confirmada", LocalDateTime.now(ZONA_BOGOTA).minusMinutes(31));
        prepararBarberoAsignado(21, 8);
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> citaService.completarCita(ID_CITA));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(citaRepository, never()).save(cita);
    }

    @Test
    void elegibilidadYFinalizacionUsanLaMismaReglaDeDuracion() {
        Cita cita = cita("confirmada", LocalDateTime.now(ZONA_BOGOTA).minusMinutes(1));
        prepararBarberoAsignado(20, ID_BARBERO);
        prepararDuracionCita(30);
        when(citaRepository.findById(ID_CITA)).thenReturn(Optional.of(cita));

        assertEquals(false, citaService.obtenerElegibilidadCompletar(ID_CITA).orElseThrow().isPuedeCompletar());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> citaService.completarCita(ID_CITA));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("No se puede completar la cita hasta que haya transcurrido la duración programada del servicio.",
                error.getReason());
        verify(citaRepository, never()).save(cita);
    }

    private Cita cita(String estado, LocalDateTime fechaHora) {
        Cita cita = new Cita();
        cita.setId(ID_CITA);
        cita.setIdUsuario(4);
        cita.setIdBarbero(ID_BARBERO);
        cita.setFecha(fechaHora.toLocalDate());
        cita.setHora(fechaHora.toLocalTime());
        cita.setEstado(estado);
        return cita;
    }

    private void prepararBarberoAsignado(int idUsuario, int idBarbero) {
        Usuario usuario = usuario(idUsuario, "barbero");
        Barbero barbero = new Barbero();
        barbero.setId(idBarbero);
        autenticar(usuario);
        when(barberoRepository.findByUsuarioId(idUsuario)).thenReturn(Optional.of(barbero));
    }

    private void prepararDuracionCita(int minutos) {
        CitaServicio relacion = new CitaServicio();
        relacion.setIdCita(ID_CITA);
        relacion.setIdServicio(11);
        Servicio servicio = new Servicio();
        servicio.setId(11);
        servicio.setDuracion(minutos);
        when(citaServicioRepository.findByIdCita(ID_CITA)).thenReturn(List.of(relacion));
        when(servicioRepository.findById(11)).thenReturn(Optional.of(servicio));
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null));
    }

    private Usuario usuario(int id, String rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRol(rol);
        return usuario;
    }
}
