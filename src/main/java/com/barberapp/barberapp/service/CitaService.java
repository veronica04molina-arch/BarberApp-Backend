package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final BarberoRepository barberoRepository;

    public CitaService(
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository,
            BarberoRepository barberoRepository) {

        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.barberoRepository = barberoRepository;
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
                fecha
        );
    }

    // Crear una cita
    public Cita guardarCita(Cita cita) {

        validarDatosCita(cita);

        // Verificar que el usuario exista y tenga rol cliente
        validarUsuarioCliente(cita.getIdUsuario());

        // Verificar que el barbero exista
        if (!barberoRepository.existsById(cita.getIdBarbero())) {
            throw new RuntimeException(
                    "El barbero indicado no existe."
            );
        }

        // Verificar disponibilidad del horario
        boolean horarioOcupado =
                citaRepository.existsByIdBarberoAndFechaAndHora(
                        cita.getIdBarbero(),
                        cita.getFecha(),
                        cita.getHora()
                );

        if (horarioOcupado) {
            throw new RuntimeException(
                    "El barbero ya tiene una cita en esa fecha y hora."
            );
        }

        // Estado inicial de la cita
        cita.setEstado("pendiente");

        // Fecha de creación
        if (cita.getFechaCreacion() == null) {
            cita.setFechaCreacion(LocalDateTime.now());
        }

        return citaRepository.save(cita);
    }

    // Actualizar una cita
    public Cita actualizarCita(
            Integer id,
            Cita datosCita) {

        Optional<Cita> citaExistente =
                citaRepository.findById(id);

        if (citaExistente.isEmpty()) {
            return null;
        }

        validarDatosCita(datosCita);

        Cita cita = citaExistente.get();

        // Verificar que el nuevo usuario exista y tenga rol cliente
        validarUsuarioCliente(datosCita.getIdUsuario());

        // Verificar que la cita pertenezca al usuario indicado
        if (!cita.getIdUsuario().equals(datosCita.  getIdUsuario())) {
            throw new RuntimeException(
                "No puedes modificar una cita que pertenece a otro usuario."
            );
        }

        // Verificar que el barbero exista
        if (!barberoRepository.existsById(
                datosCita.getIdBarbero())) {

            throw new RuntimeException(
                    "El barbero indicado no existe."
            );
        }

        // Si cambia fecha, hora o barbero,
        // comprobar que el nuevo horario esté disponible
        boolean cambioHorario =
                !cita.getIdBarbero().equals(datosCita.getIdBarbero())
                || !cita.getFecha().equals(datosCita.getFecha())
                || !cita.getHora().equals(datosCita.getHora());

        if (cambioHorario) {

            boolean horarioOcupado =
                    citaRepository.existsByIdBarberoAndFechaAndHora(
                            datosCita.getIdBarbero(),
                            datosCita.getFecha(),
                            datosCita.getHora()
                    );

            if (horarioOcupado) {
                throw new RuntimeException(
                        "El barbero ya tiene una cita en esa fecha y hora."
                );
            }
        }

        cita.setIdUsuario(datosCita.getIdUsuario());
        cita.setIdBarbero(datosCita.getIdBarbero());
        cita.setFecha(datosCita.getFecha());
        cita.setHora(datosCita.getHora());
        cita.setNotas(datosCita.getNotas());

        return citaRepository.save(cita);
    }

    // Cancelar una cita
    public boolean cancelarCita(Integer id, Integer idUsuario) {

        Optional<Cita> citaExistente =
            citaRepository.findById(id);

        if (citaExistente.isEmpty()) {
            return false;
        }

        Cita cita = citaExistente.get();

        // Verificar que la cita pertenece al usuario
        if (!cita.getIdUsuario().equals(idUsuario)) {
            throw new RuntimeException(
                "No puedes cancelar una cita que pertenece a otro usuario."
            );
        }

        // Verificar que la cita no esté cancelada
        if ("cancelada".equalsIgnoreCase(cita.getEstado())) {
            throw new RuntimeException(
                "La cita ya se encuentra cancelada."
            );
        }

        cita.setEstado("cancelada");
        citaRepository.save(cita);

        return true;
    }

    // Validar que el usuario exista y tenga rol cliente
    private void validarUsuarioCliente(Integer idUsuario) {

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElse(null);

        if (usuario == null) {
            throw new RuntimeException(
                    "El usuario indicado no existe."
            );
        }

        if (!"cliente".equalsIgnoreCase(usuario.getRol())) {
            throw new RuntimeException(
                    "Solo los usuarios con rol cliente pueden crear citas."
            );
        }
    }

    // Validaciones generales
    private void validarDatosCita(Cita cita) {

        if (cita == null) {
            throw new RuntimeException(
                    "Los datos de la cita son obligatorios."
            );
        }

        if (cita.getIdUsuario() == null) {
            throw new RuntimeException(
                    "El usuario es obligatorio."
            );
        }

        if (cita.getIdBarbero() == null) {
            throw new RuntimeException(
                    "El barbero es obligatorio."
            );
        }

        if (cita.getFecha() == null) {
            throw new RuntimeException(
                    "La fecha de la cita es obligatoria."
            );
        }

        if (cita.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException(
                    "No se puede crear una cita en una fecha anterior a hoy."
            );
        }

        if (cita.getHora() == null) {
            throw new RuntimeException(
                    "La hora de la cita es obligatoria."
            );
        }
    }
}