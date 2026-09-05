package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.ExcepcionHorario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.ExcepcionHorarioRepository;

@Service
public class ExcepcionHorarioService {

        private final ExcepcionHorarioRepository excepcionHorarioRepository;
        private final BarberoRepository barberoRepository;

        public ExcepcionHorarioService(
                        ExcepcionHorarioRepository excepcionHorarioRepository,
                        BarberoRepository barberoRepository) {

                this.excepcionHorarioRepository = excepcionHorarioRepository;
                this.barberoRepository = barberoRepository;
        }

        // LISTAR TODAS LAS EXCEPCIONES
        public List<ExcepcionHorario> listarExcepciones() {

                return excepcionHorarioRepository.findAll();
        }

        // BUSCAR EXCEPCIÓN POR ID
        public Optional<ExcepcionHorario> buscarPorId(Integer id) {

                return excepcionHorarioRepository.findById(id);
        }

        // LISTAR EXCEPCIONES DE UN BARBERO
        public List<ExcepcionHorario> listarPorBarbero(
                        Integer idBarbero) {

                validarBarbero(idBarbero);

                return excepcionHorarioRepository
                                .findByIdBarbero(idBarbero);
        }

        // LISTAR EXCEPCIONES DE UN BARBERO EN UNA FECHA
        public List<ExcepcionHorario> listarPorBarberoYFecha(
                        Integer idBarbero,
                        LocalDate fecha) {

                validarBarbero(idBarbero);

                if (fecha == null) {
                        throw new RuntimeException(
                                        "La fecha es obligatoria.");
                }

                return excepcionHorarioRepository
                                .findByIdBarberoAndFecha(
                                                idBarbero,
                                                fecha);
        }

        // CREAR EXCEPCIÓN
        public ExcepcionHorario guardarExcepcion(
                        ExcepcionHorario excepcion) {

                validarDatos(excepcion);

                validarBarbero(excepcion.getIdBarbero());

                String tipo = normalizarTipo(
                                excepcion.getTipo());

                excepcion.setTipo(tipo);

                validarHoras(excepcion);

                validarExcepcionNoSuperpuesta(excepcion);

                return excepcionHorarioRepository.save(excepcion);
        }

        // ACTUALIZAR EXCEPCIÓN
        public ExcepcionHorario actualizarExcepcion(
                        Integer id,
                        ExcepcionHorario datosExcepcion) {

                Optional<ExcepcionHorario> excepcionExistente = excepcionHorarioRepository.findById(id);

                if (excepcionExistente.isEmpty()) {
                        return null;
                }

                ExcepcionHorario excepcion = excepcionExistente.get();

                // Validar únicamente los campos
                // que se pueden modificar
                validarDatosActualizacion(datosExcepcion);

                // El tipo se puede modificar
                String tipo = normalizarTipo(
                                datosExcepcion.getTipo());

                excepcion.setTipo(tipo);

                // El motivo se puede modificar
                excepcion.setMotivo(
                                datosExcepcion.getMotivo());

                return excepcionHorarioRepository.save(excepcion);
        }

        // ELIMINAR EXCEPCIÓN
        public boolean eliminarExcepcion(Integer id) {

                Optional<ExcepcionHorario> excepcionExistente = excepcionHorarioRepository.findById(id);

                if (excepcionExistente.isEmpty()) {
                        return false;
                }

                excepcionHorarioRepository.deleteById(id);

                return true;
        }

        // VALIDAR DATOS GENERALES
        private void validarDatos(
                        ExcepcionHorario excepcion) {

                if (excepcion == null) {

                        throw new RuntimeException(
                                        "Los datos de la excepción son obligatorios.");
                }

                if (excepcion.getIdBarbero() == null) {

                        throw new RuntimeException(
                                        "El id del barbero es obligatorio.");
                }

                if (excepcion.getFecha() == null) {

                        throw new RuntimeException(
                                        "La fecha es obligatoria.");
                }

                if (excepcion.getTipo() == null
                                || excepcion.getTipo().isBlank()) {

                        throw new RuntimeException(
                                        "El tipo de excepción es obligatorio.");
                }
        }

        // VALIDAR DATOS PARA ACTUALIZACIÓN
        private void validarDatosActualizacion(
                        ExcepcionHorario datosExcepcion) {

                if (datosExcepcion == null) {

                        throw new RuntimeException(
                                        "Los datos de la excepción son obligatorios.");
                }

                if (datosExcepcion.getTipo() == null
                                || datosExcepcion.getTipo().isBlank()) {

                        throw new RuntimeException(
                                        "El tipo de excepción es obligatorio.");
                }
        }

        // VALIDAR HORAS
        private void validarHoras(
                        ExcepcionHorario excepcion) {

                LocalTime horaInicio = excepcion.getHoraInicio();

                LocalTime horaFin = excepcion.getHoraFin();

                // Si no se envía ninguna hora,
                // la excepción aplica a todo el día.
                if (horaInicio == null && horaFin == null) {
                        return;
                }

                // No se puede enviar solamente una hora
                if (horaInicio == null || horaFin == null) {

                        throw new RuntimeException(
                                        "Debe indicar la hora de inicio y la hora de finalización.");
                }

                // La hora de inicio debe ser menor
                // que la hora de finalización
                if (!horaInicio.isBefore(horaFin)) {

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

        // NORMALIZAR Y VALIDAR TIPO
        private String normalizarTipo(String tipo) {

                if (tipo == null || tipo.isBlank()) {

                        throw new RuntimeException(
                                        "El tipo de excepción es obligatorio.");
                }

                String tipoNormalizado = tipo.trim().toUpperCase();

                if (!tipoNormalizado.equals("NO_DISPONIBLE")
                                && !tipoNormalizado.equals("HORARIO_ESPECIAL")) {

                        throw new RuntimeException(
                                        "El tipo debe ser NO_DISPONIBLE o HORARIO_ESPECIAL.");
                }

                return tipoNormalizado;
        }

        // VALIDAR SUPERPOSICIÓN AL CREAR
        private void validarExcepcionNoSuperpuesta(
                        ExcepcionHorario excepcion) {

                validarExcepcionNoSuperpuesta(
                                excepcion,
                                null);
        }

        // VALIDAR SUPERPOSICIÓN AL CREAR O ACTUALIZAR
        private void validarExcepcionNoSuperpuesta(
                        ExcepcionHorario excepcion,
                        Integer idExcepcionActual) {

                List<ExcepcionHorario> excepciones = excepcionHorarioRepository
                                .findByIdBarberoAndFecha(
                                                excepcion.getIdBarbero(),
                                                excepcion.getFecha());

                for (ExcepcionHorario existente : excepciones) {

                        // En actualización ignoramos
                        // la misma excepción
                        if (idExcepcionActual != null
                                        && existente.getId()
                                                        .equals(idExcepcionActual)) {

                                continue;
                        }

                        LocalTime inicioNuevo = excepcion.getHoraInicio();

                        LocalTime finNuevo = excepcion.getHoraFin();

                        LocalTime inicioExistente = existente.getHoraInicio();

                        LocalTime finExistente = existente.getHoraFin();

                        /*
                         * Si la excepción existente es
                         * de todo el día, se considera
                         * que ocupa toda la fecha.
                         */
                        if (inicioExistente == null
                                        && finExistente == null) {

                                throw new RuntimeException(
                                                "Ya existe una excepción para todo el día en esa fecha.");
                        }

                        /*
                         * Si la nueva excepción es
                         * de todo el día, se superpone
                         * con cualquier excepción de esa fecha.
                         */
                        if (inicioNuevo == null
                                        && finNuevo == null) {

                                throw new RuntimeException(
                                                "Ya existe otra excepción en esa fecha.");
                        }

                        // Validar superposición de horarios
                        boolean seSuperpone = inicioNuevo.isBefore(finExistente)
                                        && finNuevo.isAfter(inicioExistente);

                        if (seSuperpone) {

                                throw new RuntimeException(
                                                "La excepción de horario se superpone con otra excepción existente.");
                        }
                }
        }
}