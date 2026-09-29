package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.dto.CitaClienteDTO;
import com.barberapp.barberapp.dto.CitaRequest;
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

@Service
public class CitaService {

        private final CitaRepository citaRepository;
        private final UsuarioRepository usuarioRepository;
        private final BarberoRepository barberoRepository;
        private final ServicioRepository servicioRepository;
        private final CitaServicioRepository citaServicioRepository;
        private final DisponibilidadService disponibilidadService;
        private final NotificacionService notificacionService;

        public CitaService(
                        CitaRepository citaRepository,
                        UsuarioRepository usuarioRepository,
                        BarberoRepository barberoRepository,
                        ServicioRepository servicioRepository,
                        CitaServicioRepository citaServicioRepository,
                        DisponibilidadService disponibilidadService,
                        NotificacionService notificacionService) {

                this.citaRepository = citaRepository;
                this.usuarioRepository = usuarioRepository;
                this.barberoRepository = barberoRepository;
                this.servicioRepository = servicioRepository;
                this.citaServicioRepository = citaServicioRepository;
                this.disponibilidadService = disponibilidadService;
                this.notificacionService = notificacionService;
        }

        // Listar todas las citas
        public List<Cita> listarCitas() {
                return citaRepository.findByIdBarbero(idBarberoAutenticado());
        }

        // Buscar una cita por ID
        public Optional<Cita> buscarPorId(Integer id) {
                Optional<Cita> cita = citaRepository.findById(id);
                cita.ifPresent(this::autorizarLecturaCita);
                return cita;
        }

        // Buscar citas de un usuario
        public List<CitaClienteDTO> listarCitasCliente(
                        Integer idUsuario) {

                Usuario usuarioAutenticado = usuarioAutenticado();

                if (!esCliente(usuarioAutenticado)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Esta ruta está disponible solo para clientes.");
                }

                if (!usuarioAutenticado.getId().equals(idUsuario)) {
                        throw new RuntimeException(
                                        "No puedes consultar las citas de otro usuario.");
                }

                List<Cita> citas = citaRepository.findByIdUsuario(idUsuario);

                List<CitaClienteDTO> resultado = new java.util.ArrayList<>();

                for (Cita cita : citas) {

                        // Obtener servicios de la cita
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

                        // Obtener nombre del barbero
                        String nombreBarbero = barberoRepository
                                        .findById(cita.getIdBarbero())
                                        .map(barbero -> barbero.getUsuario())
                                        .filter(java.util.Objects::nonNull)
                                        .map(Usuario::getNombre)
                                        .orElse("Barbero no disponible");

                        // Crear DTO
                        resultado.add(
                                        new CitaClienteDTO(
                                                        cita.getId(),
                                                        cita.getFecha(),
                                                        cita.getHora(),
                                                        nombresServicios,
                                                        nombreBarbero,
                                                        cita.getEstado(),
                                                        cita.getNotas()));
                }

                return resultado;
        }

        // Buscar citas de un barbero
        public List<Cita> listarPorBarbero(Integer idBarbero) {
                autorizarBarbero(idBarbero);
                return citaRepository.findByIdBarbero(idBarbero);
        }

        // Buscar citas de un barbero en una fecha
        public List<Cita> listarPorBarberoYFecha(
                        Integer idBarbero,
                        LocalDate fecha) {

                autorizarBarbero(idBarbero);
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
                Usuario autenticado = usuarioAutenticado();
                if (!esCliente(autenticado) || !autenticado.getId().equals(request.getIdUsuario())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Solo puedes crear citas para tu propio usuario.");
                }
                validarUsuarioCliente(request.getIdUsuario());

                // Verificar que el barbero exista y esté activo
                Barbero barbero = barberoRepository.findById(request.getIdBarbero())
                                .orElse(null);
                if (barbero == null) {
                        throw new RuntimeException(
                                        "El barbero indicado no existe.");
                }
                if (!"activo".equalsIgnoreCase(barbero.getEstado())) {
                        throw new RuntimeException(
                                        "El barbero indicado no está activo.");
                }

                // Verificar que los servicios sean válidos
                validarServicios(
                                request.getIdServicios(),
                                request.getIdBarbero());

                int duracionTotal = 0;
                for (Integer idServicio : request.getIdServicios()) {
                        Servicio servicio = servicioRepository.findById(idServicio)
                                        .orElseThrow();
                        duracionTotal += servicio.getDuracion();
                }

                if (!disponibilidadService.estaDisponible(
                                request.getIdBarbero(), request.getFecha(),
                                request.getHora(), duracionTotal)) {
                        throw new RuntimeException(
                                        "El horario solicitado no está disponible para este barbero.");
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

                // Crear notificación para el cliente
                notificacionService.crearNotificacion(
                                citaGuardada.getIdUsuario(),
                                citaGuardada.getId(),
                                "Tu cita ha sido registrada para el "
                                                + citaGuardada.getFecha()
                                                + " a las "
                                                + citaGuardada.getHora()
                                                + ".");

                // Crear notificación para el barbero
                notificacionService.crearNotificacion(
                                obtenerIdUsuarioBarbero(citaGuardada.getIdBarbero()),
                                citaGuardada.getId(),
                                "Tienes una nueva cita para el "
                                                + citaGuardada.getFecha()
                                                + " a las "
                                                + citaGuardada.getHora()
                                                + ".");

                return citaGuardada;
        }

        // Actualizar una cita
        @Transactional
        public Cita actualizarCita(
                        Integer id,
                        Cita datosCita) {

                Optional<Cita> citaExistente = citaRepository.findById(id);

                if (citaExistente.isEmpty()) {
                        return null;
                }

                Cita cita = citaExistente.get();

                Usuario autenticado = usuarioAutenticado();
                autorizarModificacionCita(cita, autenticado);
                validarDatosCita(datosCita);

                // No permitir modificar una cita cancelada
                if ("cancelada".equalsIgnoreCase(cita.getEstado())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "No se puede reprogramar una cita cancelada.");
                }

                // No permitir cambiar el usuario de la cita.
                if (!cita.getIdUsuario().equals(datosCita.getIdUsuario())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No puedes cambiar el usuario asignado a la cita.");
                }

                // La reprogramación no permite cambiar el barbero asignado.
                if (!cita.getIdBarbero().equals(datosCita.getIdBarbero())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "No puedes cambiar el barbero asignado a la cita.");
                }
                validarUsuarioCliente(datosCita.getIdUsuario());

                // Verificar que el barbero exista
                if (!barberoRepository.existsById(
                                datosCita.getIdBarbero())) {

                        throw new RuntimeException(
                                        "El barbero indicado no existe.");
                }

                // Verificar si cambió la fecha o la hora.
                boolean cambioHorario = !cita.getFecha().equals(
                                datosCita.getFecha())
                                || !cita.getHora().equals(
                                                datosCita.getHora());

                // Si cambió el horario, comprobar disponibilidad
                if (cambioHorario) {

                        if (!esCliente(autenticado)) {
                                throw new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "Solo el cliente puede reprogramar la cita.");
                        }

                        LocalDateTime ahora = LocalDateTime.now();
                        LocalDateTime fechaInicioActual = cita.getFecha().atTime(cita.getHora());
                        LocalDateTime fechaNueva = datosCita.getFecha().atTime(datosCita.getHora());

                        if (cita.getFechaCreacion() == null
                                        || !ahora.isBefore(cita.getFechaCreacion().plusMinutes(30))) {
                                throw new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "Solo puedes reprogramar durante los primeros 30 minutos después de crear la cita.");
                        }

                        if (!ahora.isBefore(fechaInicioActual)) {
                                throw new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "No puedes reprogramar una cita que ya comenzó.");
                        }

                        if (!fechaNueva.isAfter(ahora)) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "La nueva fecha y hora deben ser futuras.");
                        }

                        int duracionCitaActual = calcularDuracionCita(cita.getId());

                        if (!disponibilidadService.estaDisponible(
                                        datosCita.getIdBarbero(),
                                        datosCita.getFecha(),
                                        datosCita.getHora(),
                                        duracionCitaActual,
                                        cita.getId())) {

                                throw new RuntimeException(
                                                "El horario solicitado no está disponible para este barbero.");
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

                Cita citaActualizada = citaRepository.save(cita);

                if (cambioHorario) {
                        String mensaje = "Tu cita fue modificada para el "
                                        + citaActualizada.getFecha() + " a las "
                                        + citaActualizada.getHora() + ".";
                        notificacionService.crearNotificacion(
                                        citaActualizada.getIdUsuario(),
                                        citaActualizada.getId(),
                                        mensaje);

                        notificacionService.crearNotificacion(
                                        obtenerIdUsuarioBarbero(citaActualizada.getIdBarbero()),
                                        citaActualizada.getId(),
                                        "Una cita fue modificada para el "
                                                        + citaActualizada.getFecha() + " a las "
                                                        + citaActualizada.getHora() + ".");
                }

                return citaActualizada;
        }

        // Cancelar una cita
        @Transactional
        public boolean cancelarCita(
                        Integer id,
                        Integer idUsuario) {

                Optional<Cita> citaExistente = citaRepository.findById(id);

                if (citaExistente.isEmpty()) {
                        return false;
                }

                Cita cita = citaExistente.get();

                Usuario autenticado = usuarioAutenticado();
                if (!esCliente(autenticado) || !autenticado.getId().equals(cita.getIdUsuario())
                                || !autenticado.getId().equals(idUsuario)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
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

                String mensaje = "La cita del " + cita.getFecha()
                                + " a las " + cita.getHora() + " fue cancelada.";
                notificacionService.crearNotificacion(
                                cita.getIdUsuario(), cita.getId(), mensaje);
                notificacionService.crearNotificacion(
                                obtenerIdUsuarioBarbero(cita.getIdBarbero()),
                                cita.getId(),
                                "La cita del " + cita.getFecha()
                                                + " a las " + cita.getHora() + " fue cancelada.");

                return true;
        }

        public Optional<CitaClienteDTO> obtenerProximaCita(Integer idUsuario) {

                Usuario usuarioAutenticado = usuarioAutenticado();

                if (!esCliente(usuarioAutenticado)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Esta ruta está disponible solo para clientes.");
                }

                if (!usuarioAutenticado.getId().equals(idUsuario)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No puedes consultar las citas de otro usuario.");
                }

                LocalDate hoy = LocalDate.now();
                LocalTime horaActual = LocalTime.now();

                List<Cita> citas = citaRepository
                                .findByIdUsuarioAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
                                                idUsuario, hoy);

                for (Cita cita : citas) {

                        // No considerar citas canceladas
                        if ("cancelada".equalsIgnoreCase(cita.getEstado())) {
                                continue;
                        }

                        // Si es hoy, verificar que todavía no haya pasado
                        if (cita.getFecha().equals(hoy)
                                        && !cita.getHora().isAfter(horaActual)) {
                                continue;
                        }

                        List<CitaServicio> relaciones = citaServicioRepository
                                        .findByIdCita(cita.getId());

                        List<String> nombresServicios = new java.util.ArrayList<>();

                        for (CitaServicio relacion : relaciones) {

                                Servicio servicio = servicioRepository
                                                .findById(relacion.getIdServicio())
                                                .orElse(null);

                                if (servicio != null) {
                                        nombresServicios.add(servicio.getNombre());
                                }
                        }

                        String nombreBarbero = barberoRepository
                                        .findById(cita.getIdBarbero())
                                        .map(barbero -> barbero.getUsuario())
                                        .map(Usuario::getNombre)
                                        .orElse("Barbero no disponible");

                        return Optional.of(
                                        new CitaClienteDTO(
                                                        cita.getId(),
                                                        cita.getFecha(),
                                                        cita.getHora(),
                                                        nombresServicios,
                                                        nombreBarbero,
                                                        cita.getEstado(),
                                                        cita.getNotas()));
                }

                return Optional.empty();
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

        private Usuario usuarioAutenticado() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay un usuario autenticado.");
                }
                return usuario;
        }

        private boolean esCliente(Usuario usuario) {
                return "cliente".equalsIgnoreCase(usuario.getRol());
        }

        private boolean esBarbero(Usuario usuario) {
                return "barbero".equalsIgnoreCase(usuario.getRol());
        }

        private Integer idBarberoAutenticado() {
                Usuario usuario = usuarioAutenticado();
                if (!esBarbero(usuario)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol de barbero.");
                }
                return barberoRepository.findByUsuarioId(usuario.getId())
                                .map(barbero -> barbero.getId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                "No existe un registro de barbero para este usuario."));
        }

        private void autorizarBarbero(Integer idBarbero) {
                if (!idBarberoAutenticado().equals(idBarbero)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "No tienes permiso para consultar citas de este barbero.");
                }
        }

        private void autorizarLecturaCita(Cita cita) {
                Usuario usuario = usuarioAutenticado();
                if (esCliente(usuario) && usuario.getId().equals(cita.getIdUsuario()))
                        return;
                if (esBarbero(usuario) && barberoRepository.findByUsuarioId(usuario.getId())
                                .map(barbero -> barbero.getId().equals(cita.getIdBarbero())).orElse(false))
                        return;
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "No tienes permiso para consultar esta cita.");
        }

        private void autorizarModificacionCita(Cita cita, Usuario usuario) {
                if (esCliente(usuario) && usuario.getId().equals(cita.getIdUsuario()))
                        return;
                if (esBarbero(usuario) && barberoRepository.findByUsuarioId(usuario.getId())
                                .map(barbero -> barbero.getId().equals(cita.getIdBarbero())).orElse(false))
                        return;
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "No tienes permiso para modificar esta cita.");
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

                        if (servicio.getDuracion() == null || servicio.getDuracion() <= 0) {
                                throw new RuntimeException(
                                                "El servicio '" + servicio.getNombre()
                                                                + "' debe tener una duración mayor que cero.");
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
                                        "No se puede establecer una fecha anterior a hoy.");
                }

                if (request.getHora() == null) {

                        throw new RuntimeException(
                                        "La hora de la cita es obligatoria.");
                }

                if (!request.getFecha().atTime(request.getHora())
                                .isAfter(LocalDateTime.now())) {
                        throw new RuntimeException(
                                        "La fecha y hora de la cita deben ser futuras.");
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
                                        "No se puede establecer una fecha anterior a hoy.");
                }

                if (cita.getHora() == null) {

                        throw new RuntimeException(
                                        "La hora de la cita es obligatoria.");
                }
        }

        private int calcularDuracionCita(Integer idCita) {

                int total = 0;

                for (CitaServicio relacion : citaServicioRepository.findByIdCita(idCita)) {

                        Servicio servicio = servicioRepository
                                        .findById(relacion.getIdServicio())
                                        .orElse(null);

                        if (servicio != null && servicio.getDuracion() != null) {
                                total += servicio.getDuracion();
                        }
                }

                return total;
        }

        private Integer obtenerIdUsuarioBarbero(Integer idBarbero) {

                return barberoRepository.findById(idBarbero)
                                .map(barbero -> barbero.getUsuario())
                                .map(Usuario::getId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "No se encontró el usuario del barbero."));
        }
}
