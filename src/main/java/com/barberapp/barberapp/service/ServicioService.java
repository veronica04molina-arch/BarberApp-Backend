package com.barberapp.barberapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.repository.ServicioRepository;

/**
 * Contiene la lógica de negocio relacionada con los servicios.
 */
@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    /**
     * Obtiene todos los servicios registrados.
     */
    public List<Servicio> listarServicios() {
        return servicioRepository.findAll();
    }

    /**
     * Obtiene únicamente los servicios activos.
     */
    public List<Servicio> listarServiciosActivos() {
        return servicioRepository.findByEstado("activo");
    }

    /**
     * Busca un servicio por su identificador.
     */
    public Optional<Servicio> buscarPorId(Integer id) {
        return servicioRepository.findById(id);
    }

    /**
     * Guarda un nuevo servicio o actualiza uno existente.
     */
    public Servicio guardarServicio(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    /**
     * Elimina un servicio por su identificador.
     */
    public void eliminarServicio(Integer id) {
        servicioRepository.deleteById(id);
    }

    public Servicio obtenerServicio(Integer id) {
    return servicioRepository.findById(id).orElse(null);
    }

}