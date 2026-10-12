package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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

import com.barberapp.barberapp.dto.ReporteCitasDTO;
import com.barberapp.barberapp.dto.ReporteServicioDTO;
import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    private static final int ID_USUARIO_BARBERO_A = 10;
    private static final int ID_USUARIO_BARBERO_B = 15;
    private static final int ID_BARBERO_A = 1;
    private static final int ID_BARBERO_B = 2;

    @Mock private CitaRepository citaRepository;
    @Mock private CitaServicioRepository citaServicioRepository;
    @Mock private ServicioRepository servicioRepository;
    @Mock private BarberoRepository barberoRepository;
    @InjectMocks private ReporteService reporteService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resumenCuentaLosCuatroEstadosSoloDelBarberoAutenticado() {
        autenticarBarbero(ID_USUARIO_BARBERO_A, ID_BARBERO_A);
        when(citaRepository.countByIdBarbero(ID_BARBERO_A)).thenReturn(20L);
        when(citaRepository.countByIdBarberoAndEstado(ID_BARBERO_A, "pendiente")).thenReturn(3L);
        when(citaRepository.countByIdBarberoAndEstado(ID_BARBERO_A, "confirmada")).thenReturn(5L);
        when(citaRepository.countByIdBarberoAndEstado(ID_BARBERO_A, "completada")).thenReturn(10L);
        when(citaRepository.countByIdBarberoAndEstado(ID_BARBERO_A, "cancelada")).thenReturn(2L);

        ReporteCitasDTO reporte = reporteService.obtenerResumenCitas();

        assertEquals(20L, reporte.getTotalCitas());
        assertEquals(3L, reporte.getCitasPendientes());
        assertEquals(5L, reporte.getCitasConfirmadas());
        assertEquals(10L, reporte.getCitasCompletadas());
        assertEquals(2L, reporte.getCitasCanceladas());
        verify(citaRepository).countByIdBarbero(ID_BARBERO_A);
        verify(citaRepository, never()).count();
        verify(citaRepository, never()).countByEstado("pendiente");
    }

    @Test
    void barberosDiferentesObtienenResumenesDiferentes() {
        autenticarBarbero(ID_USUARIO_BARBERO_A, ID_BARBERO_A);
        when(citaRepository.countByIdBarbero(ID_BARBERO_A)).thenReturn(7L);
        configurarEstadosEnCero(ID_BARBERO_A);

        assertEquals(7L, reporteService.obtenerResumenCitas().getTotalCitas());

        autenticarBarbero(ID_USUARIO_BARBERO_B, ID_BARBERO_B);
        when(citaRepository.countByIdBarbero(ID_BARBERO_B)).thenReturn(12L);
        configurarEstadosEnCero(ID_BARBERO_B);

        assertEquals(12L, reporteService.obtenerResumenCitas().getTotalCitas());
        verify(citaRepository).countByIdBarbero(ID_BARBERO_A);
        verify(citaRepository).countByIdBarbero(ID_BARBERO_B);
    }

    @Test
    void rangoConsultaSoloLasCitasDelBarberoAutenticado() {
        autenticarBarbero(ID_USUARIO_BARBERO_A, ID_BARBERO_A);
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fin = LocalDate.of(2026, 10, 7);
        Cita cita = new Cita();
        cita.setIdBarbero(ID_BARBERO_A);
        when(citaRepository.findByIdBarberoAndFechaBetweenOrderByFechaAscHoraAsc(
                ID_BARBERO_A, inicio, fin)).thenReturn(List.of(cita));

        List<Cita> citas = reporteService.obtenerCitasPorRango(inicio, fin);

        assertEquals(List.of(cita), citas);
        assertEquals(ID_BARBERO_A, citas.getFirst().getIdBarbero());
        verify(citaRepository).findByIdBarberoAndFechaBetweenOrderByFechaAscHoraAsc(
                ID_BARBERO_A, inicio, fin);
        verify(citaRepository, never()).findByFechaBetweenOrderByFechaAscHoraAsc(inicio, fin);
    }

    @Test
    void serviciosIncluyeInactivosYCuentaSoloCitasDelBarberoAutenticado() {
        autenticarBarbero(ID_USUARIO_BARBERO_A, ID_BARBERO_A);
        Servicio corte = servicio(100, "Corte", "activo");
        Servicio barbaInactiva = servicio(101, "Barba", "inactivo");
        when(servicioRepository.findByIdBarbero(ID_BARBERO_A))
                .thenReturn(List.of(corte, barbaInactiva));
        when(citaServicioRepository.countByIdServicioAndIdBarbero(100, ID_BARBERO_A))
                .thenReturn(15L);
        when(citaServicioRepository.countByIdServicioAndIdBarbero(101, ID_BARBERO_A))
                .thenReturn(4L);

        List<ReporteServicioDTO> reporte = reporteService.obtenerServiciosMasSolicitados();

        assertEquals(List.of("Corte", "Barba"),
                reporte.stream().map(ReporteServicioDTO::getServicio).toList());
        assertEquals(List.of(15L, 4L),
                reporte.stream().map(ReporteServicioDTO::getCantidad).toList());
        verify(servicioRepository).findByIdBarbero(ID_BARBERO_A);
        verify(citaServicioRepository).countByIdServicioAndIdBarbero(100, ID_BARBERO_A);
        verify(citaServicioRepository).countByIdServicioAndIdBarbero(101, ID_BARBERO_A);
        verify(servicioRepository, never()).findAll();
        verify(citaServicioRepository, never()).countByIdServicio(100);
    }

    @Test
    void clienteNoPuedeObtenerReportes() {
        autenticarUsuario(ID_USUARIO_BARBERO_A, "cliente");

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.obtenerResumenCitas());

        assertEquals(HttpStatus.FORBIDDEN, excepcion.getStatusCode());
        verify(citaRepository, never()).countByIdBarbero(ID_BARBERO_A);
    }

    @Test
    void barberoSinRegistroAsociadoNoRecibeDatosGlobales() {
        autenticarUsuario(ID_USUARIO_BARBERO_A, "barbero");
        when(barberoRepository.findByUsuarioId(ID_USUARIO_BARBERO_A)).thenReturn(Optional.empty());

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> reporteService.obtenerResumenCitas());

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatusCode());
        verify(citaRepository, never()).count();
        verify(citaRepository, never()).countByIdBarbero(ID_BARBERO_A);
    }

    private void configurarEstadosEnCero(int idBarbero) {
        when(citaRepository.countByIdBarberoAndEstado(idBarbero, "pendiente")).thenReturn(0L);
        when(citaRepository.countByIdBarberoAndEstado(idBarbero, "confirmada")).thenReturn(0L);
        when(citaRepository.countByIdBarberoAndEstado(idBarbero, "completada")).thenReturn(0L);
        when(citaRepository.countByIdBarberoAndEstado(idBarbero, "cancelada")).thenReturn(0L);
    }

    private void autenticarBarbero(int idUsuario, int idBarbero) {
        autenticarUsuario(idUsuario, "barbero");
        Barbero barbero = new Barbero();
        barbero.setId(idBarbero);
        when(barberoRepository.findByUsuarioId(idUsuario)).thenReturn(Optional.of(barbero));
    }

    private void autenticarUsuario(int idUsuario, String rol) {
        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        usuario.setRol(rol);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private Servicio servicio(int id, String nombre, String estado) {
        Servicio servicio = new Servicio();
        servicio.setId(id);
        servicio.setIdBarbero(ID_BARBERO_A);
        servicio.setNombre(nombre);
        servicio.setEstado(estado);
        return servicio;
    }
}
