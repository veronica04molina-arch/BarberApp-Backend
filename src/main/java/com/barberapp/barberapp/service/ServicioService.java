package com.barberapp.barberapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.ServicioRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final BarberoRepository barberoRepository;
    private final UsuarioRepository usuarioRepository;

    public ServicioService(
            ServicioRepository servicioRepository,
            BarberoRepository barberoRepository,
            UsuarioRepository usuarioRepository) {

        this.servicioRepository = servicioRepository;
        this.barberoRepository = barberoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Servicio> listarServicios() {
        return servicioRepository.findAll();
    }

    public List<Servicio> listarServiciosActivos() {
        return servicioRepository.findByEstado("activo");
    }

    public Optional<Servicio> buscarPorId(Integer id) {
        return servicioRepository.findById(id);
    }

    public Servicio obtenerServicio(Integer id) {
        return servicioRepository.findById(id).orElse(null);
    }

    public Servicio guardarServicio(Servicio servicio) {

        if (servicio == null) {
            throw new RuntimeException(
                    "El servicio no puede ser nulo.");
        }

        if (servicio.getNombre() == null ||
                servicio.getNombre().isBlank()) {

            throw new RuntimeException(
                    "El nombre del servicio es obligatorio.");
        }

        if (servicio.getPrecio() == null) {
            throw new RuntimeException(
                    "El precio del servicio es obligatorio.");
        }

        if (servicio.getDuracion() == null) {
            throw new RuntimeException(
                    "La duración del servicio es obligatoria.");
        }

        if (servicio.getDuracion() <= 0) {
            throw new RuntimeException(
                    "La duración del servicio debe ser mayor que cero.");
        }

        if (servicio.getDescripcion() == null ||
                servicio.getDescripcion().isBlank()) {

            throw new RuntimeException(
                    "La descripción del servicio es obligatoria.");
        }

        // Obtener el barbero autenticado mediante JWT
        Barbero barberoAutenticado = obtenerBarberoAutenticado();

        // El idBarbero viene del JWT, no del request
        servicio.setIdBarbero(barberoAutenticado.getId());

        String nombre = servicio.getNombre().trim();
        servicio.setNombre(nombre);

        boolean duplicado = servicioRepository
                .existsByIdBarberoAndNombreIgnoreCase(
                        barberoAutenticado.getId(),
                        nombre);

        if (duplicado) {
            throw new RuntimeException(
                    "Ese barbero ya tiene un servicio con ese nombre.");
        }

        if (servicio.getEstado() == null ||
                servicio.getEstado().isBlank()) {

            servicio.setEstado("activo");
        }

        return servicioRepository.save(servicio);
    }

    public Servicio actualizarServicio(
            Integer id,
            Servicio datosServicio) {

        Optional<Servicio> servicioExistente = servicioRepository.findById(id);

        if (servicioExistente.isEmpty()) {
            return null;
        }

        Servicio servicio = servicioExistente.get();

        validarDatosActualizacion(datosServicio);

        // Obtener el barbero autenticado
        Barbero barberoAutenticado = obtenerBarberoAutenticado();

        // Verificar que el servicio pertenezca al barbero
        if (!servicio.getIdBarbero()
                .equals(barberoAutenticado.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tiene permiso para modificar este servicio.");
        }

        servicio.setPrecio(datosServicio.getPrecio());
        servicio.setDuracion(datosServicio.getDuracion());
        servicio.setDescripcion(datosServicio.getDescripcion());

        return servicioRepository.save(servicio);
    }

    private void validarDatosActualizacion(
            Servicio servicio) {

        if (servicio == null) {
            throw new RuntimeException(
                    "Los datos del servicio son obligatorios.");
        }

        if (servicio.getPrecio() == null) {
            throw new RuntimeException(
                    "El precio del servicio es obligatorio.");
        }

        if (servicio.getDuracion() == null) {
            throw new RuntimeException(
                    "La duración del servicio es obligatoria.");
        }

        if (servicio.getDuracion() <= 0) {
            throw new RuntimeException(
                    "La duración del servicio debe ser mayor que cero.");
        }

        if (servicio.getDescripcion() == null ||
                servicio.getDescripcion().isBlank()) {

            throw new RuntimeException(
                    "La descripción del servicio es obligatoria.");
        }
    }

    public boolean desactivarServicio(Integer id) {

        Optional<Servicio> servicioExistente = servicioRepository.findById(id);

        if (servicioExistente.isEmpty()) {
            return false;
        }

        Servicio servicio = servicioExistente.get();

        // Obtener el barbero autenticado
        Barbero barberoAutenticado = obtenerBarberoAutenticado();

        // Verificar propiedad del servicio
        if (!servicio.getIdBarbero()
                .equals(barberoAutenticado.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tiene permiso para desactivar este servicio.");
        }

        servicio.setEstado("inactivo");
        servicioRepository.save(servicio);

        return true;
    }

    /**
     * Obtiene el barbero correspondiente al usuario
     * que está autenticado mediante JWT.
     */

    private Barbero obtenerBarberoAutenticado() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No hay un usuario autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Usuario)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No se pudo obtener el usuario autenticado.");
        }

        Usuario usuario = (Usuario) principal;

        if (!"barbero".equalsIgnoreCase(usuario.getRol())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene rol de barbero.");
        }

        Barbero barbero = barberoRepository
                .findByUsuarioId(usuario.getId())
                .orElse(null);

        if (barbero == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe un registro de barbero para este usuario.");
        }

        return barbero;
    }

}
