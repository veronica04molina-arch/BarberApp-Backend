package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barberapp.barberapp.dto.CitaClienteDTO;
import com.barberapp.barberapp.dto.CitaRequest;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@Service
public class CitaService {

        private final CitaRepository citaRepository;
        private final UsuarioRepository usuarioRepository;
        private final BarberoRepository barberoRepository;
        private final ServicioRepository servicioRepository;
        private final CitaServicioRepository citaServicioRepository;

        public CitaService(
                        CitaRepository citaRepository,
                        UsuarioRepository usuarioRepository,
                        BarberoRepository barberoRepository,
                        ServicioRepository servicioRepository,
                        CitaServicioRepository citaServicioRepository) {

                this.citaRepository = citaRepository;
                this.usuarioRepository = usuarioRepository;
                this.barberoRepository = barberoRepository;
                this.servicioRepository = servicioRepository;
                this.citaServicioRepository = citaServicioRepository;
        }

        // Listar todas las citas
        public List<Cita> listarCitas() {
                return citaRepository.findAll();
        }

        // Buscar una cita por ID
        public Optional<Cita> buscarPorId(Integer id) {
                return citaRepository.findById(id);
        }

        // Buscar citas de un usuario
        public List<CitaClienteDTO> listarCitasCliente(
                        Integer idUsuario) {

                List<Cita> citas = citaRepository.findByIdUsuario(idUsuario);

                List<CitaClienteDTO> resultado = new java.util.ArrayList<>();

                for (Cita cita : citas) {

                        List<CitaServicio> relaciones = citaServicioRepository.findByIdCita(
                                        cita.getId());

                        List<String> nombresServicios = new java.util.ArrayList<>();

                        for (CitaServicio relacion : relaciones) {

                                Servicio servicio = servicioRepository.findById(
                                                relacion.getIdServicio())
                                                .orElse(null);

                                if (servicio != null) {
                                        nombresServicios.add(
                                                        servicio.getNombre());
                                }
                        }

                        resultado.add(
                                        new CitaClienteDTO(
                                                        cita.getId(),
                                                        cita.getFecha(),
                                                        cita.getHora(),
                                                        nombresServicios,
                                                        cita.getEstado(),
                                                        cita.getNotas()));
                }

                return resultado;
        }

        public List<Cita> listarPorUsuario(Integer idUsuario) {
                return citaRepository.findByIdUsuario(idUsuario);
        }

        // Buscar citas de un barbero
        public List<Cita> listarPorBarbero(Integer idBarbero) {
                return citaRepository.findByIdBarbero(idBarbero);
        }

        // Buscar citas de un barbero en una fecha
        public List<Cita> listarPorBarberoYFecha(
                        Integer idBarbero,
                        LocalDate fecha) {

                return citaRepository.findByIdBarberoAndFecha(
                                idBarbero,
                                fecha);
        }

        // Crear una cita con uno, dos o tres servicios
        @Transactional
        public Cita guardarCita(CitaRequest request) {

                // Validar los datos generales de la cita
                validarDatosCita(request);

                // Verificar que el usuario exista y tenga rol cliente
                validarUsuarioCliente(request.getIdUsuario());

                // Verificar que el barbero exista
                if (!barberoRepository.existsById(request.getIdBarbero())) {

                        throw new RuntimeException(
                                        "El barbero indicado no existe.");
                }

                // Verificar que los servicios sean válidos
                validarServicios(
                                request.getIdServicios(),
                                request.getIdBarbero());

                // Verificar disponibilidad del horario
                boolean horarioOcupado = citaRepository.existsByIdBarberoAndFechaAndHora(
                                request.getIdBarbero(),
                                request.getFecha(),
                                request.getHora());

                if (horarioOcupado) {

                        throw new RuntimeException(
                                        "El barbero ya tiene una cita en esa fecha y hora.");
                }

                // Crear el objeto Cita
                Cita cita = new Cita();

                cita.setIdUsuario(request.getIdUsuario());
                cita.setIdBarbero(request.getIdBarbero());
                cita.setFecha(request.getFecha());
                cita.setHora(request.getHora());
                cita.setNotas(request.getNotas());

                // Estado inicial de la cita
                cita.setEstado("pendiente");

                // Fecha de creación
                cita.setFechaCreacion(LocalDateTime.now());

                // Guardar la cita en la tabla cita
                Cita citaGuardada = citaRepository.save(cita);

                // Guardar los servicios seleccionados (máximo 3)
                for (Integer idServicio : request.getIdServicios()) {

                        CitaServicio citaServicio = new CitaServicio();

                        citaServicio.setIdCita(citaGuardada.getId());
                        citaServicio.setIdServicio(idServicio);

                        citaServicioRepository.save(citaServicio);
                }

                return citaGuardada;
        }

        // Actualizar una cita
        public Cita actualizarCita(
                        Integer id,
                        Cita datosCita) {

                Optional<Cita> citaExistente = citaRepository.findById(id);

                if (citaExistente.isEmpty()) {
                        return null;
                }

                validarDatosCita(datosCita);

                Cita cita = citaExistente.get();

                // Verificar que el nuevo usuario exista y tenga rol cliente
                validarUsuarioCliente(
                                datosCita.getIdUsuario());

                // Verificar que la cita pertenezca al usuario indicado
                if (!cita.getIdUsuario().equals(
                                datosCita.getIdUsuario())) {

                        throw new RuntimeException(
                                        "No puedes modificar una cita que pertenece a otro usuario.");
                }

                // Verificar que el barbero exista
                if (!barberoRepository.existsById(
                                datosCita.getIdBarbero())) {

                        throw new RuntimeException(
                                        "El barbero indicado no existe.");
                }

                /*
                 * Verificar si cambió el barbero,
                 * la fecha o la hora.
                 */
                boolean cambioHorario = !cita.getIdBarbero().equals(
                                datosCita.getIdBarbero())
                                || !cita.getFecha().equals(
                                                datosCita.getFecha())
                                || !cita.getHora().equals(
                                                datosCita.getHora());

                // Si cambió el horario, comprobar disponibilidad
                if (cambioHorario) {

                        boolean horarioOcupado = citaRepository.existsByIdBarberoAndFechaAndHora(
                                        datosCita.getIdBarbero(),
                                        datosCita.getFecha(),
                                        datosCita.getHora());

                        if (horarioOcupado) {

                                throw new RuntimeException(
                                                "El barbero ya tiene una cita en esa fecha y hora.");
                        }
                }

                // Actualizar los datos de la cita
                cita.setIdUsuario(
                                datosCita.getIdUsuario());

                cita.setIdBarbero(
                                datosCita.getIdBarbero());

                cita.setFecha(
                                datosCita.getFecha());

                cita.setHora(
                                datosCita.getHora());

                cita.setNotas(
                                datosCita.getNotas());

                return citaRepository.save(cita);
        }

        // Cancelar una cita
        public boolean cancelarCita(
                        Integer id,
                        Integer idUsuario) {

                Optional<Cita> citaExistente = citaRepository.findById(id);

                if (citaExistente.isEmpty()) {
                        return false;
                }

                Cita cita = citaExistente.get();

                // Verificar que la cita pertenece al usuario
                if (!cita.getIdUsuario().equals(idUsuario)) {

                        throw new RuntimeException(
                                        "No puedes cancelar una cita que pertenece a otro usuario.");
                }

                // Verificar que la cita no esté cancelada
                if ("cancelada".equalsIgnoreCase(
                                cita.getEstado())) {

                        throw new RuntimeException(
                                        "La cita ya se encuentra cancelada.");
                }

                // Cambiar el estado de la cita
                cita.setEstado("cancelada");

                citaRepository.save(cita);

                return true;
        }

        // Validar que el usuario exista y tenga rol cliente
        private void validarUsuarioCliente(
                        Integer idUsuario) {

                Usuario usuario = usuarioRepository
                                .findById(idUsuario)
                                .orElse(null);

                if (usuario == null) {

                        throw new RuntimeException(
                                        "El usuario indicado no existe.");
                }

                if (!"cliente".equalsIgnoreCase(
                                usuario.getRol())) {

                        throw new RuntimeException(
                                        "Solo los usuarios con rol cliente pueden crear citas.");
                }
        }

        // Validar los servicios seleccionados.
        private void validarServicios(
                        List<Integer> idServicios,
                        Integer idBarbero) {

                // Verificar que se haya seleccionado al menos un servicio
                if (idServicios == null ||
                                idServicios.isEmpty()) {

                        throw new RuntimeException(
                                        "Debe seleccionar al menos un servicio.");
                }

                // Verificar que no se seleccionen más de 3 servicios
                if (idServicios.size() > 3) {

                        throw new RuntimeException(
                                        "Una cita puede tener máximo 3 servicios.");
                }

                // Verificar que no se repita el mismo servicio.
                Set<Integer> serviciosUnicos = new HashSet<>(idServicios);

                if (serviciosUnicos.size() != idServicios.size()) {

                        throw new RuntimeException(
                                        "No se puede seleccionar el mismo servicio más de una vez.");
                }

                // Validar cada servicio seleccionado.
                for (Integer idServicio : idServicios) {

                        // Verificar que el ID del servicio no sea null
                        if (idServicio == null) {

                                throw new RuntimeException(
                                                "El ID del servicio es obligatorio.");
                        }

                        // Buscar el servicio en la base de datos
                        Servicio servicio = servicioRepository
                                        .findById(idServicio)
                                        .orElse(null);

                        // Verificar que el servicio exista
                        if (servicio == null) {

                                throw new RuntimeException(
                                                "El servicio con ID "
                                                                + idServicio
                                                                + " no existe.");
                        }

                        // Verificar que el servicio esté activo
                        if (!"activo".equalsIgnoreCase(
                                        servicio.getEstado())) {

                                throw new RuntimeException(
                                                "El servicio '"
                                                                + servicio.getNombre()
                                                                + "' no está disponible.");
                        }

                        // Verificar que el servicio pertenezca al barbero seleccionado.
                        if (!servicio.getIdBarbero().equals(
                                        idBarbero)) {

                                throw new RuntimeException(
                                                "El servicio '"
                                                                + servicio.getNombre()
                                                                + "' no pertenece al barbero seleccionado.");
                        }
                }
        }

        // Validaciones generales para crear una cita
        private void validarDatosCita(
                        CitaRequest request) {

                if (request == null) {

                        throw new RuntimeException(
                                        "Los datos de la cita son obligatorios.");
                }

                if (request.getIdUsuario() == null) {

                        throw new RuntimeException(
                                        "El usuario es obligatorio.");
                }

                if (request.getIdBarbero() == null) {

                        throw new RuntimeException(
                                        "El barbero es obligatorio.");
                }

                if (request.getFecha() == null) {

                        throw new RuntimeException(
                                        "La fecha de la cita es obligatoria.");
                }

                // No permitir fechas anteriores al día actual
                if (request.getFecha().isBefore(
                                LocalDate.now())) {

                        throw new RuntimeException(
                                        "No se puede crear una cita en una fecha anterior a hoy.");
                }

                if (request.getHora() == null) {

                        throw new RuntimeException(
                                        "La hora de la cita es obligatoria.");
                }
        }

        // Validaciones generales para actualizar una cita
        private void validarDatosCita(
                        Cita cita) {

                if (cita == null) {

                        throw new RuntimeException(
                                        "Los datos de la cita son obligatorios.");
                }

                if (cita.getIdUsuario() == null) {

                        throw new RuntimeException(
                                        "El usuario es obligatorio.");
                }

                if (cita.getIdBarbero() == null) {

                        throw new RuntimeException(
                                        "El barbero es obligatorio.");
                }

                if (cita.getFecha() == null) {

                        throw new RuntimeException(
                                        "La fecha de la cita es obligatoria.");
                }

                // No permitir fechas anteriores al día actual
                if (cita.getFecha().isBefore(
                                LocalDate.now())) {

                        throw new RuntimeException(
                                        "No se puede crear una cita en una fecha anterior a hoy.");
                }

                if (cita.getHora() == null) {

                        throw new RuntimeException(
                                        "La hora de la cita es obligatoria.");
                }
        }
}