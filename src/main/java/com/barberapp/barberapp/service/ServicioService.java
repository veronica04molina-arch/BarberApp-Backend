package com.barberapp.barberapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final BarberoRepository barberoRepository;

    public ServicioService(ServicioRepository servicioRepository,
                            BarberoRepository barberoRepository) {
        this.servicioRepository = servicioRepository;
        this.barberoRepository = barberoRepository;
    }

    // Obtener todos los servicios
    public List<Servicio> listarServicios() {
        return servicioRepository.findAll();
    }

    // Obtener únicamente los servicios activos
    public List<Servicio> listarServiciosActivos() {
        return servicioRepository.findByEstado("activo");
    }

    // Buscar un servicio por ID
    public Optional<Servicio> buscarPorId(Integer id) {
        return servicioRepository.findById(id);
    }

    // Obtener un servicio por ID
    public Servicio obtenerServicio(Integer id) {
        return servicioRepository.findById(id).orElse(null);
    }

    // Registrar un servicio
    public Servicio guardarServicio(Servicio servicio) {

        if (servicio == null) {
            throw new RuntimeException(
                    "El servicio no puede ser nulo."
            );
        }

        if (servicio.getNombre() == null ||
                servicio.getNombre().isBlank()) {

            throw new RuntimeException(
                    "El nombre del servicio es obligatorio."
            );
        }

        // Validar que el barbero exista
        // y que no tenga otro servicio con el mismo nombre
        validarBarberoYServicioDuplicado(servicio);

        if (servicio.getPrecio() == null) {
            throw new RuntimeException(
                    "El precio del servicio es obligatorio."
            );
        }

        if (servicio.getDuracion() == null) {
            throw new RuntimeException(
                    "La duración del servicio es obligatoria."
            );
        }

        if (servicio.getDescripcion() == null ||
                servicio.getDescripcion().isBlank()) {

            throw new RuntimeException(
                    "La descripción del servicio es obligatoria."
            );
        }

        // Si no se envía estado, queda activo
        if (servicio.getEstado() == null ||
                servicio.getEstado().isBlank()) {

            servicio.setEstado("activo");
        }

        return servicioRepository.save(servicio);
    }

    // Actualizar un servicio
    // Solo se pueden modificar precio, duración y descripción
    public Servicio actualizarServicio(
            Integer id,
            Servicio datosServicio) {

        Optional<Servicio> servicioExistente =
                servicioRepository.findById(id);

        if (servicioExistente.isEmpty()) {
            return null;
        }

        Servicio servicio = servicioExistente.get();

        validarDatosActualizacion(datosServicio);

        // Solo se actualizan estos datos
        servicio.setPrecio(datosServicio.getPrecio());
        servicio.setDuracion(datosServicio.getDuracion());
        servicio.setDescripcion(datosServicio.getDescripcion());

        return servicioRepository.save(servicio);
    }

    // Validar datos permitidos para actualizar
    private void validarDatosActualizacion(Servicio servicio) {

        if (servicio == null) {
            throw new RuntimeException(
                    "Los datos del servicio son obligatorios."
            );
        }

        if (servicio.getPrecio() == null) {
            throw new RuntimeException(
                    "El precio del servicio es obligatorio."
            );
        }

        if (servicio.getDuracion() == null) {
            throw new RuntimeException(
                    "La duración del servicio es obligatoria."
            );
        }

        if (servicio.getDescripcion() == null ||
                servicio.getDescripcion().isBlank()) {

            throw new RuntimeException(
                    "La descripción del servicio es obligatoria."
            );
        }
    }

    // Validar barbero y evitar servicios duplicados
    // Esta validación se utiliza únicamente al registrar
    private void validarBarberoYServicioDuplicado(
            Servicio servicio) {

        if (servicio.getIdBarbero() == null) {
            throw new RuntimeException(
                    "El id del barbero es obligatorio."
            );
        }

        if (!barberoRepository.existsById(
                servicio.getIdBarbero())) {

            throw new RuntimeException(
                    "El barbero indicado no existe."
            );
        }

        String nombre = servicio.getNombre().trim();
        servicio.setNombre(nombre);

        boolean duplicado =
                servicioRepository
                        .existsByIdBarberoAndNombreIgnoreCase(
                                servicio.getIdBarbero(),
                                nombre);

        if (duplicado) {
            throw new RuntimeException(
                    "Ese barbero ya tiene un servicio con ese nombre."
            );
        }
    }

    // Desactivar un servicio
    public boolean desactivarServicio(Integer id) {

        Optional<Servicio> servicioExistente =
                servicioRepository.findById(id);

        if (servicioExistente.isEmpty()) {
            return false;
        }

        Servicio servicio = servicioExistente.get();

        servicio.setEstado("inactivo");

        servicioRepository.save(servicio);

        return true;
    }
}