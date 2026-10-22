package com.barberapp.barberapp.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.ExcepcionHorario;
import com.barberapp.barberapp.model.HorarioSemanal;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ExcepcionHorarioRepository;
import com.barberapp.barberapp.repository.HorarioSemanalRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@Service
public class DisponibilidadService {

    private final BarberoRepository barberoRepository;
    private final HorarioSemanalRepository horarioSemanalRepository;
    private final ExcepcionHorarioRepository excepcionHorarioRepository;
    private final CitaRepository citaRepository;
    private final CitaServicioRepository citaServicioRepository;
    private final ServicioRepository servicioRepository;

    public DisponibilidadService(
            BarberoRepository barberoRepository,
            HorarioSemanalRepository horarioSemanalRepository,
            ExcepcionHorarioRepository excepcionHorarioRepository,
            CitaRepository citaRepository,
            CitaServicioRepository citaServicioRepository,
            ServicioRepository servicioRepository) {

        this.barberoRepository = barberoRepository;
        this.horarioSemanalRepository = horarioSemanalRepository;
        this.excepcionHorarioRepository = excepcionHorarioRepository;
        this.citaRepository = citaRepository;
        this.citaServicioRepository = citaServicioRepository;
        this.servicioRepository = servicioRepository;
    }

    // VALIDAR SI UN HORARIO ESPECÍFICO ESTÁ DISPONIBLE
    public boolean estaDisponible(
            Integer idBarbero,
            LocalDate fecha,
            LocalTime hora,
            int duracion) {

        return estaDisponible(
                idBarbero,
                fecha,
                hora,
                duracion,
                null);
    }

    public boolean estaDisponible(
            Integer idBarbero,
            LocalDate fecha,
            LocalTime hora,
            int duracion,
            Integer idCitaIgnorar) {

        // Validaciones básicas
        if (idBarbero == null
                || fecha == null
                || hora == null
                || duracion <= 0
                || barberoRepository.findById(idBarbero)
                                .filter(barbero -> "activo".equalsIgnoreCase(barbero.getEstado()))
                                .isEmpty()) {

            return false;
        }

        // Validar que la cita no termine después de medianoche
        if (!fecha.atTime(hora).isAfter(java.time.LocalDateTime.now())) {
            return false;
        }

        int minutosDesdeMedianoche = hora.getHour() * 60 + hora.getMinute();

        if (duracion > 24 * 60 - minutosDesdeMedianoche) {
            return false;
        }

        LocalTime fin = hora.plusMinutes(duracion);

        // Obtener el día de la semana
        String dia = nombreDia(fecha.getDayOfWeek());

        // HORARIO SEMANAL
        List<HorarioSemanal> horarios = horarioSemanalRepository
                .findByIdBarberoAndDiaSemanaAndEstado(
                        idBarbero,
                        dia,
                        "disponible");

        // EXCEPCIONES DE HORARIO
        List<ExcepcionHorario> excepciones = excepcionHorarioRepository
                .findByIdBarberoAndFecha(
                        idBarbero,
                        fecha);

        // VALIDAR HORARIO SEMANAL
        List<ExcepcionHorario> horariosEspeciales = excepciones.stream()
                .filter(e -> "HORARIO_ESPECIAL"
                        .equalsIgnoreCase(e.getTipo()))
                .toList();

        boolean dentroDelHorarioBase;
        if (horariosEspeciales.isEmpty()) {
            dentroDelHorarioBase = horarios.stream().anyMatch(h -> !hora.isBefore(h.getHoraInicio())
                    && !fin.isAfter(h.getHoraFin()));
        } else {
            dentroDelHorarioBase = horariosEspeciales.stream().anyMatch(e -> intervaloContiene(
                        e.getHoraInicio(),
                        e.getHoraFin(),
                        hora,
                        fin));
        }

        // Si no está dentro del horario normal
        // ni dentro de un horario especial, no está disponible
        if (!dentroDelHorarioBase) {

            return false;
        }

        // VALIDAR BLOQUEOS NO DISPONIBLES
        for (ExcepcionHorario excepcion : excepciones) {

            if (!"NO_DISPONIBLE"
                    .equalsIgnoreCase(excepcion.getTipo())) {

                continue;
            }

            // Excepción de día completo
            if (excepcion.getHoraInicio() == null
                    && excepcion.getHoraFin() == null) {

                return false;
            }

            // Excepción de un intervalo específico
            if (seSuperpone(
                    hora,
                    fin,
                    excepcion.getHoraInicio(),
                    excepcion.getHoraFin())) {

                return false;
            }
        }

        // VALIDAR CITAS EXISTENTES
        List<Cita> citas = citaRepository.findByIdBarberoAndFecha(
                idBarbero,
                fecha);

        for (Cita existente : citas) {

            // En actualización, ignorar la cita actual
            if (idCitaIgnorar != null
                    && existente.getId().equals(idCitaIgnorar)) {

                continue;
            }

            // Las citas canceladas no bloquean el horario
            if ("cancelada"
                    .equalsIgnoreCase(existente.getEstado())) {

                continue;
            }

            // Calcular duración de la cita existente
            int duracionExistente = duracionCita(existente.getId());

            if (duracionExistente <= 0) {
                continue;
            }

            LocalTime finExistente = existente.getHora()
                    .plusMinutes(duracionExistente);

            // Verificar si existe superposición
            if (seSuperpone(
                    hora,
                    fin,
                    existente.getHora(),
                    finExistente)) {

                return false;
            }
        }

        return true;
    }

    // OBTENER HORARIOS DISPONIBLES PARA UN DÍA
    public List<LocalTime> obtenerHorariosDisponibles(
            Integer idBarbero,
            LocalDate fecha,
            int duracion) {

        return obtenerHorariosDisponibles(
                idBarbero,
                fecha,
                duracion,
                null);
    }

    public List<LocalTime> obtenerHorariosDisponibles(
            Integer idBarbero,
            LocalDate fecha,
            int duracion,
            Integer idCitaIgnorar) {

        List<LocalTime> horariosDisponibles = new ArrayList<>();

        // Validaciones básicas
        if (idBarbero == null
                || fecha == null
                || duracion <= 0) {

            return horariosDisponibles;
        }

        DisponibilidadContexto contexto = cargarContexto(idBarbero, fecha, idCitaIgnorar);
        if (contexto == null) {
            return horariosDisponibles;
        }

        // Empezar desde las 00:00
        LocalTime hora = LocalTime.MIDNIGHT;

        // Revisar horarios cada 30 minutos
        while (hora.isBefore(LocalTime.of(23, 30))) {

            if (estaDisponible(contexto, hora, duracion)) {

                horariosDisponibles.add(hora);
            }

            hora = hora.plusMinutes(30);
        }

        // Ordenar los horarios
        horariosDisponibles.sort(
                Comparator.naturalOrder());

        return horariosDisponibles;
    }

    // CALCULAR DURACIÓN DE UNA CITA
    private int duracionCita(Integer idCita) {

        int total = 0;

        List<CitaServicio> relaciones = citaServicioRepository
                .findByIdCita(idCita);

        for (CitaServicio relacion : relaciones) {
            Servicio servicio = servicioRepository
                    .findById(relacion.getIdServicio())
                    .orElse(null);

            if (servicio != null && servicio.getDuracion() != null) {
                total += servicio.getDuracion();
            }
        }

        return total;
    }

    // VALIDAR SI UN INTERVALO CONTIENE OTRO
    private boolean intervaloContiene(
            LocalTime inicioBloque,
            LocalTime finBloque,
            LocalTime inicioCita,
            LocalTime finCita) {

        // Excepción de día completo
        return inicioBloque != null
                && finBloque != null
                && !inicioCita.isBefore(inicioBloque)
                && !finCita.isAfter(finBloque);
    }

    // VALIDAR SUPERPOSICIÓN DE INTERVALOS
    private boolean seSuperpone(
            LocalTime inicioA,
            LocalTime finA,
            LocalTime inicioB,
            LocalTime finB) {

        return inicioB != null
                && finB != null
                && inicioA.isBefore(finB)
                && finA.isAfter(inicioB);
    }

    // CONVERTIR DÍA DE LA SEMANA
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

    private DisponibilidadContexto cargarContexto(
            Integer idBarbero,
            LocalDate fecha,
            Integer idCitaIgnorar) {

        Barbero barbero = barberoRepository.findById(idBarbero).orElse(null);
        if (barbero == null || !"activo".equalsIgnoreCase(barbero.getEstado())) {
            return null;
        }

        List<HorarioSemanal> horarios = horarioSemanalRepository
                .findByIdBarberoAndDiaSemanaAndEstado(
                        idBarbero,
                        nombreDia(fecha.getDayOfWeek()),
                        "disponible");
        List<ExcepcionHorario> excepciones = excepcionHorarioRepository
                .findByIdBarberoAndFecha(idBarbero, fecha);
        List<Cita> citas = citaRepository.findByIdBarberoAndFecha(idBarbero, fecha).stream()
                .filter(cita -> !"cancelada".equalsIgnoreCase(cita.getEstado()))
                .filter(cita -> idCitaIgnorar == null || !idCitaIgnorar.equals(cita.getId()))
                .toList();

        return new DisponibilidadContexto(
                fecha, horarios, excepciones, citas, duracionesCitas(citas));
    }

    /** Carga relaciones y servicios en lotes, no una vez por cita ni por franja. */
    private Map<Integer, Integer> duracionesCitas(Collection<Cita> citas) {
        Set<Integer> idCitas = new HashSet<>();
        for (Cita cita : citas) {
            if (cita.getId() != null) {
                idCitas.add(cita.getId());
            }
        }
        if (idCitas.isEmpty()) {
            return Map.of();
        }

        List<CitaServicio> relaciones = citaServicioRepository.findByIdCitaIn(idCitas);
        Set<Integer> idServicios = new HashSet<>();
        for (CitaServicio relacion : relaciones) {
            if (relacion.getIdServicio() != null) {
                idServicios.add(relacion.getIdServicio());
            }
        }

        Map<Integer, Integer> duracionPorServicio = new HashMap<>();
        for (Servicio servicio : servicioRepository.findAllById(idServicios)) {
            if (servicio.getDuracion() != null) {
                duracionPorServicio.put(servicio.getId(), servicio.getDuracion());
            }
        }

        Map<Integer, Integer> duracionPorCita = new HashMap<>();
        for (CitaServicio relacion : relaciones) {
            Integer duracion = duracionPorServicio.get(relacion.getIdServicio());
            if (duracion != null && relacion.getIdCita() != null) {
                duracionPorCita.merge(relacion.getIdCita(), duracion, Integer::sum);
            }
        }
        return duracionPorCita;
    }

    private boolean estaDisponible(
            DisponibilidadContexto contexto,
            LocalTime hora,
            int duracion) {

        if (hora == null
                || duracion <= 0
                || !contexto.fecha().atTime(hora).isAfter(java.time.LocalDateTime.now())) {
            return false;
        }

        int minutosDesdeMedianoche = hora.getHour() * 60 + hora.getMinute();
        if (duracion > 24 * 60 - minutosDesdeMedianoche) {
            return false;
        }
        LocalTime fin = hora.plusMinutes(duracion);

        List<ExcepcionHorario> horariosEspeciales = contexto.excepciones().stream()
                .filter(e -> "HORARIO_ESPECIAL".equalsIgnoreCase(e.getTipo()))
                .toList();
        boolean dentroDelHorarioBase = horariosEspeciales.isEmpty()
                ? contexto.horarios().stream().anyMatch(h -> !hora.isBefore(h.getHoraInicio())
                        && !fin.isAfter(h.getHoraFin()))
                : horariosEspeciales.stream().anyMatch(e -> intervaloContiene(
                        e.getHoraInicio(), e.getHoraFin(), hora, fin));
        if (!dentroDelHorarioBase) {
            return false;
        }

        for (ExcepcionHorario excepcion : contexto.excepciones()) {
            if (!"NO_DISPONIBLE".equalsIgnoreCase(excepcion.getTipo())) {
                continue;
            }
            if (excepcion.getHoraInicio() == null && excepcion.getHoraFin() == null) {
                return false;
            }
            if (seSuperpone(hora, fin, excepcion.getHoraInicio(), excepcion.getHoraFin())) {
                return false;
            }
        }

        for (Cita existente : contexto.citas()) {
            int duracionExistente = contexto.duracionPorCita().getOrDefault(existente.getId(), 0);
            if (duracionExistente > 0 && seSuperpone(
                    hora,
                    fin,
                    existente.getHora(),
                    existente.getHora().plusMinutes(duracionExistente))) {
                return false;
            }
        }
        return true;
    }

    private record DisponibilidadContexto(
            LocalDate fecha,
            List<HorarioSemanal> horarios,
            List<ExcepcionHorario> excepciones,
            List<Cita> citas,
            Map<Integer, Integer> duracionPorCita) {
    }
}
