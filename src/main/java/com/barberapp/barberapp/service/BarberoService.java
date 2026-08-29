package com.barberapp.barberapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@Service
public class BarberoService {

    private final BarberoRepository barberoRepository;
    private final UsuarioRepository usuarioRepository;

    public BarberoService(BarberoRepository barberoRepository, UsuarioRepository usuarioRepository) {
        this.barberoRepository = barberoRepository;
        this.usuarioRepository = usuarioRepository;
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

    if (barbero == null || barbero.getUsuario() == null
            || barbero.getUsuario().getId() == null) {

        throw new RuntimeException("Debe seleccionar un usuario.");
    }

    Integer idUsuario = barbero.getUsuario().getId();

    Optional<com.barberapp.barberapp.model.Usuario> usuarioOptional =
            usuarioRepository.findById(idUsuario);

    if (usuarioOptional.isEmpty()) {
        throw new RuntimeException("El usuario no existe.");
    }

    com.barberapp.barberapp.model.Usuario usuario = usuarioOptional.get();

    if (!"barbero".equalsIgnoreCase(usuario.getRol())) {
        throw new RuntimeException(
                "El usuario seleccionado no tiene el rol de barbero."
        );
    }

    if (barberoRepository.existsByUsuarioId(idUsuario)) {
        throw new RuntimeException(
                "Este usuario ya está registrado como barbero."
        );
    }

    barbero.setUsuario(usuario);

    if (barbero.getEstado() == null || barbero.getEstado().isBlank()) {
        barbero.setEstado("activo");
    }

    return barberoRepository.save(barbero);
}

    // Actualizar un barbero
    public Barbero actualizarBarbero(Integer id, Barbero datosBarbero) {

    Optional<Barbero> barberoExistente = barberoRepository.findById(id);

    if (barberoExistente.isPresent()) {

        Barbero barbero = barberoExistente.get();

        barbero.setEspecialidad(datosBarbero.getEspecialidad());
        barbero.setExperiencia(datosBarbero.getExperiencia());

        return barberoRepository.save(barbero);
    }

    return null;
}

    // Desactivar un barbero
    public boolean desactivarBarbero(Integer id) {

    Optional<Barbero> barberoExistente =
            barberoRepository.findById(id);

    if (barberoExistente.isEmpty()) {
        return false;
    }

    Barbero barbero = barberoExistente.get();
    barbero.setEstado("inactivo");

    barberoRepository.save(barbero);

    return true;
}
}