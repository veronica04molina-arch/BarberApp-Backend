package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.HorarioSemanal;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ExcepcionHorarioRepository;
import com.barberapp.barberapp.repository.HorarioSemanalRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceReprogramacionTest {

    private static final int ID_BARBERO = 7;
    private static final int ID_CITA_REPROGRAMADA = 31;
    private static final int ID_OTRA_CITA = 32;

    @Mock private BarberoRepository barberoRepository;
    @Mock private HorarioSemanalRepository horarioSemanalRepository;
    @Mock private ExcepcionHorarioRepository excepcionHorarioRepository;
    @Mock private CitaRepository citaRepository;
    @Mock private CitaServicioRepository citaServicioRepository;
    @Mock private ServicioRepository servicioRepository;

    @InjectMocks private DisponibilidadService disponibilidadService;

    private LocalDate fecha;

    @BeforeEach
    void prepararDisponibilidadBase() {
        fecha = LocalDate.now().plusDays(2);

        Barbero barbero = new Barbero();
        barbero.setId(ID_BARBERO);
        barbero.setEstado("activo");
        when(barberoRepository.findById(ID_BARBERO)).thenReturn(Optional.of(barbero));
        when(horarioSemanalRepository.findByIdBarberoAndDiaSemanaAndEstado(
                ID_BARBERO, nombreDia(fecha.getDayOfWeek()), "disponible"))
                .thenReturn(List.of(horarioLaboral()));
        when(excepcionHorarioRepository.findByIdBarberoAndFecha(ID_BARBERO, fecha))
                .thenReturn(List.of());

        CitaServicio relacion = new CitaServicio();
        relacion.setIdServicio(11);
        lenient().when(citaServicioRepository.findByIdCita(anyInt())).thenReturn(List.of(relacion));

        Servicio servicio = new Servicio();
        servicio.setId(11);
        servicio.setDuracion(30);
        lenient().when(servicioRepository.findById(11)).thenReturn(Optional.of(servicio));
    }

    @Test
    void liberaLaHoraAnteriorYMantieneOcupadaLaNuevaTrasReprogramar() {
        when(citaRepository.findByIdBarberoAndFecha(ID_BARBERO, fecha))
                .thenReturn(List.of(cita(ID_CITA_REPROGRAMADA, LocalTime.of(9, 0))));

        assertTrue(disponibilidadService.estaDisponible(
                ID_BARBERO, fecha, LocalTime.of(8, 0), 30));
        assertFalse(disponibilidadService.estaDisponible(
                ID_BARBERO, fecha, LocalTime.of(9, 0), 30));
    }

    @Test
    void noLiberaLaHoraAnteriorSiOtraCitaLaOcupa() {
        when(citaRepository.findByIdBarberoAndFecha(ID_BARBERO, fecha))
                .thenReturn(List.of(
                        cita(ID_OTRA_CITA, LocalTime.of(8, 0)),
                        cita(ID_CITA_REPROGRAMADA, LocalTime.of(9, 0))));

        assertFalse(disponibilidadService.estaDisponible(
                ID_BARBERO, fecha, LocalTime.of(8, 0), 30));
        assertFalse(disponibilidadService.estaDisponible(
                ID_BARBERO, fecha, LocalTime.of(9, 0), 30));
    }

    @Test
    void soloIgnoraLaCitaDuranteLaSeleccionPreviaALaReprogramacion() {
        when(citaRepository.findByIdBarberoAndFecha(ID_BARBERO, fecha))
                .thenReturn(List.of(cita(ID_CITA_REPROGRAMADA, LocalTime.of(9, 0))));

        assertTrue(disponibilidadService.estaDisponible(
                ID_BARBERO, fecha, LocalTime.of(9, 0), 30, ID_CITA_REPROGRAMADA));
        assertFalse(disponibilidadService.estaDisponible(
                ID_BARBERO, fecha, LocalTime.of(9, 0), 30));
    }

    @Test
    void calculaElDiaConConsultasAgrupadasEnLugarDeUnaPorFranja() {
        Cita primera = cita(ID_OTRA_CITA, LocalTime.of(8, 0));
        Cita segunda = cita(ID_CITA_REPROGRAMADA, LocalTime.of(9, 0));
        when(citaRepository.findByIdBarberoAndFecha(ID_BARBERO, fecha))
                .thenReturn(List.of(primera, segunda));

        CitaServicio servicioPrimera = relacion(ID_OTRA_CITA, 11);
        CitaServicio servicioSegunda = relacion(ID_CITA_REPROGRAMADA, 12);
        when(citaServicioRepository.findByIdCitaIn(anyCollection()))
                .thenReturn(List.of(servicioPrimera, servicioSegunda));

        Servicio segundoServicio = new Servicio();
        segundoServicio.setId(12);
        segundoServicio.setDuracion(30);
        when(servicioRepository.findAllById(any())).thenReturn(List.of(servicio(), segundoServicio));

        List<LocalTime> horarios = disponibilidadService.obtenerHorariosDisponibles(
                ID_BARBERO, fecha, 30);

        assertFalse(horarios.contains(LocalTime.of(8, 0)));
        assertFalse(horarios.contains(LocalTime.of(9, 0)));
        assertTrue(horarios.contains(LocalTime.of(9, 30)));
        verify(barberoRepository, times(1)).findById(ID_BARBERO);
        verify(horarioSemanalRepository, times(1))
                .findByIdBarberoAndDiaSemanaAndEstado(
                        ID_BARBERO, nombreDia(fecha.getDayOfWeek()), "disponible");
        verify(excepcionHorarioRepository, times(1)).findByIdBarberoAndFecha(ID_BARBERO, fecha);
        verify(citaRepository, times(1)).findByIdBarberoAndFecha(ID_BARBERO, fecha);
        verify(citaServicioRepository, times(1)).findByIdCitaIn(anyCollection());
        verify(servicioRepository, times(1)).findAllById(any());
        verify(citaServicioRepository, never()).findByIdCita(anyInt());
        verify(servicioRepository, never()).findById(anyInt());
    }

    private HorarioSemanal horarioLaboral() {
        HorarioSemanal horario = new HorarioSemanal();
        horario.setIdBarbero(ID_BARBERO);
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(10, 0));
        horario.setEstado("disponible");
        return horario;
    }

    private Cita cita(int id, LocalTime hora) {
        Cita cita = new Cita();
        cita.setId(id);
        cita.setIdBarbero(ID_BARBERO);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado("pendiente");
        return cita;
    }

    private CitaServicio relacion(int idCita, int idServicio) {
        CitaServicio relacion = new CitaServicio();
        relacion.setIdCita(idCita);
        relacion.setIdServicio(idServicio);
        return relacion;
    }

    private Servicio servicio() {
        Servicio servicio = new Servicio();
        servicio.setId(11);
        servicio.setDuracion(30);
        return servicio;
    }

    private String nombreDia(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }
}
