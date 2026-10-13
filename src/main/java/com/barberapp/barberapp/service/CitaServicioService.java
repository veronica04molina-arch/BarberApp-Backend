package com.barberapp.barberapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

@Service
public class CitaServicioService {

    private final CitaServicioRepository citaServicioRepository;
    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final BarberoRepository barberoRepository;

    public CitaServicioService(
            CitaServicioRepository citaServicioRepository,
            CitaRepository citaRepository,
            ServicioRepository servicioRepository,
            BarberoRepository barberoRepository) {

        this.citaServicioRepository = citaServicioRepository;
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.barberoRepository = barberoRepository;
    }

    // Listar todas las relaciones cita-servicio
    public List<CitaServicio> listarCitaServicios() {
        return citaServicioRepository.findAll().stream()
                .filter(this::puedeAccederRelacion)
                .toList();
    }

    // Buscar una relación por su ID
    public Optional<CitaServicio> buscarPorId(Integer id) {
        Optional<CitaServicio> relacion = citaServicioRepository.findById(id);
        relacion.ifPresent(this::autorizarAccesoRelacion);
        return relacion;
    }

    // Listar los servicios asociados a una cita
    public List<CitaServicio> listarPorCita(Integer idCita) {
        autorizarAccesoCita(obtenerCita(idCita));
        return citaServicioRepository.findByIdCita(idCita);
    }

    // Listar las citas que utilizan un servicio
    public List<CitaServicio> listarPorServicio(Integer idServicio) {
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El servicio indicado no existe."));
        autorizarBarberoPropietario(servicio.getIdBarbero());
        return citaServicioRepository.findByIdServicio(idServicio);
    }

    // Asociar un servicio a una cita
    public CitaServicio guardarCitaServicio(CitaServicio citaServicio) {

        validarDatos(citaServicio);

        // Verificar que la cita exista
        Cita cita = citaRepository
                .findById(citaServicio.getIdCita())
                .orElse(null);

        if (cita == null) {
            throw new RuntimeException(
                    "La cita indicada no existe."
            );
        }
        autorizarAccesoCita(cita);

        // Verificar que la cita no esté cancelada
        if ("cancelada".equalsIgnoreCase(cita.getEstado())) {
            throw new RuntimeException(
                    "No se pueden agregar servicios a una cita cancelada."
            );
        }

        // Verificar que el servicio exista
        Servicio servicio = servicioRepository
                .findById(citaServicio.getIdServicio())
                .orElse(null);

        if (servicio == null) {
            throw new RuntimeException(
                    "El servicio indicado no existe."
            );
        }

        // Verificar que el servicio esté activo
        if (!"activo".equalsIgnoreCase(servicio.getEstado())) {
            throw new RuntimeException(
                    "No se puede agregar un servicio inactivo."
            );
        }

        // Validación del barbero
        if (!cita.getIdBarbero().equals(servicio.getIdBarbero())) {
            throw new RuntimeException(
                "El servicio no pertenece al barbero de la cita."
            );
        }

        // Evitar servicios duplicados en la misma cita
        boolean existe = citaServicioRepository
                .existsByIdCitaAndIdServicio(
                        citaServicio.getIdCita(),
                        citaServicio.getIdServicio()
                );

        if (existe) {
            throw new RuntimeException(
                    "El servicio ya está asociado a esta cita."
            );
        }

        // Guardar el precio y la descripción actuales únicamente al crear
        // la relación. Las relaciones existentes nunca se recalculan.
        citaServicio.setPrecioAplicado(servicio.getPrecio());
        citaServicio.setDescripcionAplicada(servicio.getDescripcion());

        return citaServicioRepository.save(citaServicio);
    }

    // Eliminar un servicio de una cita
    public boolean eliminarCitaServicio(Integer id) {

        Optional<CitaServicio> relacion =
                citaServicioRepository.findById(id);

        if (relacion.isEmpty()) {
            return false;
        }

        autorizarAccesoRelacion(relacion.get());
        citaServicioRepository.deleteById(id);
        return true;
    }

    // Validar los datos recibidos
    private void validarDatos(CitaServicio citaServicio) {

        if (citaServicio == null) {
            throw new RuntimeException(
                    "Los datos de la relación son obligatorios."
            );
        }

        if (citaServicio.getIdCita() == null) {
            throw new RuntimeException(
                    "El id de la cita es obligatorio."
            );
        }

        if (citaServicio.getIdServicio() == null) {
            throw new RuntimeException(
                    "El id del servicio es obligatorio."
            );
        }
    }

    private Cita obtenerCita(Integer idCita) {
        return citaRepository.findById(idCita)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La cita indicada no existe."));
    }

    private boolean puedeAccederRelacion(CitaServicio relacion) {
        Cita cita = citaRepository.findById(relacion.getIdCita()).orElse(null);
        return cita != null && tieneAccesoCita(cita);
    }

    private void autorizarAccesoRelacion(CitaServicio relacion) {
        autorizarAccesoCita(obtenerCita(relacion.getIdCita()));
    }

    private void autorizarAccesoCita(Cita cita) {
        if (!tieneAccesoCita(cita)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para acceder a los servicios de esta cita.");
        }
    }

    private boolean tieneAccesoCita(Cita cita) {
        Usuario usuario = usuarioAutenticado();
        if ("cliente".equalsIgnoreCase(usuario.getRol())) {
            return usuario.getId().equals(cita.getIdUsuario());
        }
        if ("barbero".equalsIgnoreCase(usuario.getRol())) {
            return barberoRepository.findByUsuarioId(usuario.getId())
                    .map(barbero -> barbero.getId().equals(cita.getIdBarbero()))
                    .orElse(false);
        }
        return false;
    }

    private void autorizarBarberoPropietario(Integer idBarbero) {
        Usuario usuario = usuarioAutenticado();
        if (!"barbero".equalsIgnoreCase(usuario.getRol())
                || barberoRepository.findByUsuarioId(usuario.getId())
                        .map(Barbero::getId)
                        .filter(idBarbero::equals)
                        .isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para consultar las citas de este servicio.");
        }
    }

    private Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay un usuario autenticado.");
        }
        return usuario;
    }
}
