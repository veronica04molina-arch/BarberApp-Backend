package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.ExcepcionHorario;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ExcepcionHorarioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@ExtendWith(MockitoExtension.class)
class ExcepcionHorarioServiceTest {

        private static final Integer ID_BARBERO = 7;
        private static final LocalDate FECHA = LocalDate.of(2026, 10, 5);

        @Mock
        private ExcepcionHorarioRepository excepcionHorarioRepository;

        @Mock
        private BarberoRepository barberoRepository;

        @Mock
        private CitaRepository citaRepository;

        @Mock
        private CitaServicioRepository citaServicioRepository;

        @Mock
        private ServicioRepository servicioRepository;

        @InjectMocks
        private ExcepcionHorarioService excepcionHorarioService;

        private void prepararCreacion() {
                when(barberoRepository.existsById(ID_BARBERO)).thenReturn(true);
                when(excepcionHorarioRepository.findByIdBarberoAndFecha(ID_BARBERO, FECHA))
                                .thenReturn(List.of());
        }

        @Test
        void rechazaExcepcionDeTodoElDiaCuandoHayCitaPendiente() {
                prepararCreacion();
                when(citaRepository.findByIdBarberoAndFecha(ID_BARBERO, FECHA))
                                .thenReturn(List.of(cita("pendiente", LocalTime.of(17, 30))));

                ExcepcionHorario excepcion = excepcion("NO_DISPONIBLE", null, null);

                assertThrows(RuntimeException.class,
                                () -> excepcionHorarioService.guardarExcepcion(excepcion));

                verify(excepcionHorarioRepository, never()).save(any());
        }

        @Test
        void rechazaExcepcionDesde1731CuandoCitaDe1730Dura20Minutos() {
                prepararCreacion();
                Cita cita = cita("confirmada", LocalTime.of(17, 30));
                prepararCitaConDuracionDeVeinteMinutos(cita);

                ExcepcionHorario excepcion = excepcion(
                                "NO_DISPONIBLE",
                                LocalTime.of(17, 31),
                                LocalTime.of(18, 0));

                assertThrows(RuntimeException.class,
                                () -> excepcionHorarioService.guardarExcepcion(excepcion));

                verify(excepcionHorarioRepository, never()).save(any());
        }

        @Test
        void permiteExcepcionDesde1750CuandoCitaDe1730Dura20Minutos() {
                prepararCreacion();
                prepararCitaConDuracionDeVeinteMinutos(cita("confirmada", LocalTime.of(17, 30)));

                ExcepcionHorario excepcion = excepcion(
                                "NO_DISPONIBLE",
                                LocalTime.of(17, 50),
                                LocalTime.of(18, 0));
                when(excepcionHorarioRepository.save(excepcion)).thenReturn(excepcion);

                assertDoesNotThrow(() -> excepcionHorarioService.guardarExcepcion(excepcion));

                verify(excepcionHorarioRepository).save(excepcion);
        }

        @Test
        void rechazaExcepcionDesde1749CuandoCitaDe1730Dura20Minutos() {
                prepararCreacion();
                prepararCitaConDuracionDeVeinteMinutos(cita("confirmada", LocalTime.of(17, 30)));

                ExcepcionHorario excepcion = excepcion(
                                "NO_DISPONIBLE",
                                LocalTime.of(17, 49),
                                LocalTime.of(18, 0));

                assertThrows(RuntimeException.class,
                                () -> excepcionHorarioService.guardarExcepcion(excepcion));

                verify(excepcionHorarioRepository, never()).save(any());
        }

        @Test
        void permiteExcepcionQueTerminaAlInicioDeLaCita() {
                prepararCreacion();
                prepararCitaConDuracionDeVeinteMinutos(cita("confirmada", LocalTime.of(17, 30)));

                ExcepcionHorario excepcion = excepcion(
                                "NO_DISPONIBLE",
                                LocalTime.of(17, 0),
                                LocalTime.of(17, 30));
                when(excepcionHorarioRepository.save(excepcion)).thenReturn(excepcion);

                assertDoesNotThrow(() -> excepcionHorarioService.guardarExcepcion(excepcion));

                verify(excepcionHorarioRepository).save(excepcion);
        }

        @Test
        void rechazaExcepcionQueTerminaDespuesDelInicioDeLaCita() {
                prepararCreacion();
                prepararCitaConDuracionDeVeinteMinutos(cita("confirmada", LocalTime.of(17, 30)));

                ExcepcionHorario excepcion = excepcion(
                                "NO_DISPONIBLE",
                                LocalTime.of(17, 0),
                                LocalTime.of(17, 31));

                assertThrows(RuntimeException.class,
                                () -> excepcionHorarioService.guardarExcepcion(excepcion));

                verify(excepcionHorarioRepository, never()).save(any());
        }

        @Test
        void permiteExcepcionDeTodoElDiaCuandoLasCitasEstanCanceladasOCompletadas() {
                prepararCreacion();
                when(citaRepository.findByIdBarberoAndFecha(ID_BARBERO, FECHA)).thenReturn(List.of(
                                cita("cancelada", LocalTime.of(10, 0)),
                                cita("completada", LocalTime.of(15, 0))));

                ExcepcionHorario excepcion = excepcion("NO_DISPONIBLE", null, null);
                when(excepcionHorarioRepository.save(excepcion)).thenReturn(excepcion);

                assertDoesNotThrow(() -> excepcionHorarioService.guardarExcepcion(excepcion));

                verify(excepcionHorarioRepository).save(excepcion);
        }

        @Test
        void rechazaActualizacionQueConflictaConUnaCitaPendiente() {
                ExcepcionHorario existente = excepcion("NO_DISPONIBLE", null, null);
                existente.setId(8);
                ExcepcionHorario cambios = excepcion("NO_DISPONIBLE", null, null);

                when(excepcionHorarioRepository.findById(8)).thenReturn(Optional.of(existente));
                when(citaRepository.findByIdBarberoAndFecha(ID_BARBERO, FECHA))
                                .thenReturn(List.of(cita("pendiente", LocalTime.of(9, 0))));

                assertThrows(RuntimeException.class,
                                () -> excepcionHorarioService.actualizarExcepcion(8, cambios));

                verify(excepcionHorarioRepository, never()).save(any());
        }

        private ExcepcionHorario excepcion(String tipo, LocalTime horaInicio, LocalTime horaFin) {
                ExcepcionHorario excepcion = new ExcepcionHorario();
                excepcion.setIdBarbero(ID_BARBERO);
                excepcion.setFecha(FECHA);
                excepcion.setTipo(tipo);
                excepcion.setHoraInicio(horaInicio);
                excepcion.setHoraFin(horaFin);
                return excepcion;
        }

        private Cita cita(String estado, LocalTime hora) {
                Cita cita = new Cita();
                cita.setEstado(estado);
                cita.setHora(hora);
                return cita;
        }

        private void prepararCitaConDuracionDeVeinteMinutos(Cita cita) {
                cita.setId(31);

                CitaServicio relacion = new CitaServicio();
                relacion.setIdCita(31);
                relacion.setIdServicio(11);

                Servicio servicio = new Servicio();
                servicio.setId(11);
                servicio.setDuracion(20);

                when(citaRepository.findByIdBarberoAndFecha(ID_BARBERO, FECHA)).thenReturn(List.of(cita));
                when(citaServicioRepository.findByIdCita(31)).thenReturn(List.of(relacion));
                when(servicioRepository.findById(11)).thenReturn(Optional.of(servicio));
        }
}
