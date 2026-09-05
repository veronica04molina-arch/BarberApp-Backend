package com.barberapp.barberapp.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.HorarioSemanal;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.HorarioSemanalRepository;

@Service
public class HorarioSemanalService {

        private final HorarioSemanalRepository horarioSemanalRepository;
        private final BarberoRepository barberoRepository;

        public HorarioSemanalService(
                        HorarioSemanalRepository horarioSemanalRepository,
                        BarberoRepository barberoRepository) {

                this.horarioSemanalRepository = horarioSemanalRepository;
                this.barberoRepository = barberoRepository;
        }

        // LISTAR TODOS LOS HORARIOS
        public List<HorarioSemanal> listarHorarios() {

                return horarioSemanalRepository.findAll();
        }

        // BUSCAR HORARIO POR ID
        public Optional<HorarioSemanal> buscarPorId(Integer id) {

                return horarioSemanalRepository.findById(id);
        }

        // LISTAR HORARIOS DE UN BARBERO
        public List<HorarioSemanal> listarPorBarbero(
                        Integer idBarbero) {

                validarBarbero(idBarbero);

                return horarioSemanalRepository
                                .findByIdBarbero(idBarbero);
        }

        // LISTAR HORARIOS DE UN BARBERO Y DÍA
        public List<HorarioSemanal> listarPorBarberoYDia(
                        Integer idBarbero,
                        String diaSemana) {

                validarBarbero(idBarbero);

                String dia = normalizarDia(diaSemana);

                return horarioSemanalRepository
                                .findByIdBarberoAndDiaSemana(
                                                idBarbero,
                                                dia);
        }

        // LISTAR HORARIOS DISPONIBLES
        public List<HorarioSemanal> listarDisponibles(
                        Integer idBarbero,
                        String diaSemana) {

                validarBarbero(idBarbero);

                String dia = normalizarDia(diaSemana);

                return horarioSemanalRepository
                                .findByIdBarberoAndDiaSemanaAndEstado(
                                                idBarbero,
                                                dia,
                                                "disponible");
        }

        // CREAR HORARIO
        public HorarioSemanal guardarHorario(
                        HorarioSemanal horario) {

                validarDatos(horario);

                validarBarbero(horario.getIdBarbero());

                String dia = normalizarDia(
                                horario.getDiaSemana());

                horario.setDiaSemana(dia);

                String estado = normalizarEstado(
                                horario.getEstado());

                horario.setEstado(estado);

                validarHorarioNoSuperpuesto(horario);

                return horarioSemanalRepository.save(horario);
        }

        // ACTUALIZAR HORARIO
        public HorarioSemanal actualizarHorario(
                        Integer id,
                        HorarioSemanal datosHorario) {

                Optional<HorarioSemanal> horarioExistente = horarioSemanalRepository.findById(id);

                // Si no existe el horario
                if (horarioExistente.isEmpty()) {
                        return null;
                }

                HorarioSemanal horario = horarioExistente.get();

                // Validar los datos que se pueden modificar
                validarDatosActualizacion(datosHorario);

                // Normalizar día
                String dia = normalizarDia(
                                datosHorario.getDiaSemana());

                datosHorario.setDiaSemana(dia);

                // Si no se envía estado,
                // conservar el estado actual
                String estado = datosHorario.getEstado();

                if (estado == null || estado.isBlank()) {

                        estado = horario.getEstado();

                } else {

                        estado = normalizarEstado(estado);
                }

                datosHorario.setEstado(estado);

                // El barbero NO se modifica.
                // Se conserva el que ya tenía el horario.
                datosHorario.setIdBarbero(
                                horario.getIdBarbero());

                // Validar que no se superponga
                // con otro horario
                validarHorarioNoSuperpuesto(
                                datosHorario,
                                id);

                // Actualizar únicamente
                // los campos permitidos

                horario.setDiaSemana(
                                datosHorario.getDiaSemana());

                horario.setHoraInicio(
                                datosHorario.getHoraInicio());

                horario.setHoraFin(
                                datosHorario.getHoraFin());

                horario.setEstado(
                                datosHorario.getEstado());

                return horarioSemanalRepository.save(horario);
        }

        // ELIMINAR HORARIO
        public boolean eliminarHorario(Integer id) {

                Optional<HorarioSemanal> horarioExistente = horarioSemanalRepository.findById(id);

                if (horarioExistente.isEmpty()) {
                        return false;
                }

                horarioSemanalRepository.deleteById(id);

                return true;
        }

        // VALIDAR DATOS PARA CREAR
        private void validarDatos(
                        HorarioSemanal horario) {

                if (horario == null) {

                        throw new RuntimeException(
                                        "Los datos del horario son obligatorios.");
                }

                if (horario.getIdBarbero() == null) {

                        throw new RuntimeException(
                                        "El id del barbero es obligatorio.");
                }

                validarDatosActualizacion(horario);
        }

        // VALIDAR DATOS PARA ACTUALIZAR
        private void validarDatosActualizacion(
                        HorarioSemanal horario) {

                if (horario == null) {

                        throw new RuntimeException(
                                        "Los datos del horario son obligatorios.");
                }

                // Validar día
                if (horario.getDiaSemana() == null
                                || horario.getDiaSemana().isBlank()) {

                        throw new RuntimeException(
                                        "El día de la semana es obligatorio.");
                }

                // Validar hora de inicio
                if (horario.getHoraInicio() == null) {

                        throw new RuntimeException(
                                        "La hora de inicio es obligatoria.");
                }

                // Validar hora de finalización
                if (horario.getHoraFin() == null) {

                        throw new RuntimeException(
                                        "La hora de finalización es obligatoria.");
                }

                // Validar que inicio sea menor que fin
                if (!horario.getHoraInicio()
                                .isBefore(horario.getHoraFin())) {

                        throw new RuntimeException(
                                        "La hora de inicio debe ser menor que la hora de finalización.");
                }
        }

        // VALIDAR QUE EL BARBERO EXISTA
        private void validarBarbero(Integer idBarbero) {

                if (!barberoRepository.existsById(idBarbero)) {

                        throw new RuntimeException(
                                        "El barbero indicado no existe.");
                }
        }

        // NORMALIZAR Y VALIDAR DÍA
        private String normalizarDia(
                        String diaSemana) {

                if (diaSemana == null
                                || diaSemana.isBlank()) {

                        throw new RuntimeException(
                                        "El día de la semana es obligatorio.");
                }

                String dia = diaSemana
                                .trim()
                                .toUpperCase();

                if (!dia.equals("LUNES")
                                && !dia.equals("MARTES")
                                && !dia.equals("MIERCOLES")
                                && !dia.equals("JUEVES")
                                && !dia.equals("VIERNES")
                                && !dia.equals("SABADO")
                                && !dia.equals("DOMINGO")) {

                        throw new RuntimeException(
                                        "El día de la semana no es válido.");
                }

                return dia;
        }

        // NORMALIZAR Y VALIDAR ESTADO
        private String normalizarEstado(
                        String estado) {

                if (estado == null
                                || estado.isBlank()) {

                        return "disponible";
                }

                String estadoNormalizado = estado
                                .trim()
                                .toLowerCase();

                if (!estadoNormalizado.equals("disponible")
                                && !estadoNormalizado.equals("no_disponible")) {

                        throw new RuntimeException(
                                        "El estado debe ser disponible o no_disponible.");
                }

                return estadoNormalizado;
        }

        // VALIDAR HORARIOS SUPERPUESTOS
        private void validarHorarioNoSuperpuesto(
                        HorarioSemanal horario) {

                validarHorarioNoSuperpuesto(
                                horario,
                                null);
        }

        private void validarHorarioNoSuperpuesto(
                        HorarioSemanal horario,
                        Integer idHorarioActual) {

                List<HorarioSemanal> horarios = horarioSemanalRepository
                                .findByIdBarberoAndDiaSemana(
                                                horario.getIdBarbero(),
                                                horario.getDiaSemana());

                for (HorarioSemanal existente : horarios) {

                        // En una actualización,
                        // ignoramos el mismo horario
                        if (idHorarioActual != null
                                        && existente.getId()
                                                        .equals(idHorarioActual)) {

                                continue;
                        }

                        LocalTime inicioNuevo = horario.getHoraInicio();

                        LocalTime finNuevo = horario.getHoraFin();

                        LocalTime inicioExistente = existente.getHoraInicio();

                        LocalTime finExistente = existente.getHoraFin();

                        boolean seSuperpone = inicioNuevo.isBefore(finExistente)
                                        && finNuevo.isAfter(inicioExistente);

                        if (seSuperpone) {

                                throw new RuntimeException(
                                                "El horario se superpone con otro horario existente.");
                        }
                }
        }
}