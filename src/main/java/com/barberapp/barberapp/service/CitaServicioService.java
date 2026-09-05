package com.barberapp.barberapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@Service
public class CitaServicioService {

    private final CitaServicioRepository citaServicioRepository;
    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;

    public CitaServicioService(
            CitaServicioRepository citaServicioRepository,
            CitaRepository citaRepository,
            ServicioRepository servicioRepository) {

        this.citaServicioRepository = citaServicioRepository;
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
    }

    // Listar todas las relaciones cita-servicio
    public List<CitaServicio> listarCitaServicios() {
        return citaServicioRepository.findAll();
    }

    // Buscar una relación por su ID
    public Optional<CitaServicio> buscarPorId(Integer id) {
        return citaServicioRepository.findById(id);
    }

    // Listar los servicios asociados a una cita
    public List<CitaServicio> listarPorCita(Integer idCita) {
        return citaServicioRepository.findByIdCita(idCita);
    }

    // Listar las citas que utilizan un servicio
    public List<CitaServicio> listarPorServicio(Integer idServicio) {
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

        return citaServicioRepository.save(citaServicio);
    }

    // Eliminar un servicio de una cita
    public boolean eliminarCitaServicio(Integer id) {

        Optional<CitaServicio> relacion =
                citaServicioRepository.findById(id);

        if (relacion.isEmpty()) {
            return false;
        }

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
}