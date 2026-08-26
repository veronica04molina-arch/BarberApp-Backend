package com.barberapp.barberapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.repository.BarberoRepository;

@Service
public class BarberoService {

    private final BarberoRepository barberoRepository;

    public BarberoService(BarberoRepository barberoRepository) {
        this.barberoRepository = barberoRepository;
    }

    // Obtener todos los barberos
    public List<Barbero> listarBarberos() {
        return barberoRepository.findAll();
    }

    // Obtener un barbero por ID
    public Optional<Barbero> buscarPorId(Integer id) {
        return barberoRepository.findById(id);
    }

    // Guardar un barbero
    public Barbero guardarBarbero(Barbero barbero) {
        return barberoRepository.save(barbero);
    }

    // Actualizar un barbero
    public Barbero actualizarBarbero(Integer id, Barbero datosBarbero) {

        Optional<Barbero> barberoExistente = barberoRepository.findById(id);

        if (barberoExistente.isPresent()) {

            Barbero barbero = barberoExistente.get();

            barbero.setUsuario(datosBarbero.getUsuario());
            barbero.setEspecialidad(datosBarbero.getEspecialidad());
            barbero.setExperiencia(datosBarbero.getExperiencia());
            barbero.setEstado(datosBarbero.getEstado());

            return barberoRepository.save(barbero);
        }

        return null;
    }

    // Eliminar un barbero
    public void eliminarBarbero(Integer id) {
        barberoRepository.deleteById(id);
    }
}