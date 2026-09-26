package com.barberapp.barberapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.model.Notificacion;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.NotificacionRepository;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(
            NotificacionRepository notificacionRepository) {

        this.notificacionRepository = notificacionRepository;
    }

    // LISTAR NOTIFICACIONES DEL USUARIO
    public List<Notificacion> listarPorUsuario(Integer idUsuario) {

        Usuario usuario = usuarioAutenticado();

        if (!usuario.getId().equals(idUsuario)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes consultar las notificaciones de otro usuario.");
        }

        return notificacionRepository
                .findByIdUsuarioOrderByFechaEnvioDesc(idUsuario);
    }

    // MARCAR NOTIFICACIÓN COMO LEÍDA
    public Notificacion marcarComoLeida(Integer id) {

        Usuario usuario = usuarioAutenticado();

        Notificacion notificacion = notificacionRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La notificación no existe."));

        if (!usuario.getId().equals(notificacion.getIdUsuario())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes modificar una notificación de otro usuario.");
        }

        notificacion.setEstado("leida");

        return notificacionRepository.save(notificacion);
    }

    // CREAR NOTIFICACIÓN
    public Notificacion crearNotificacion(
            Integer idUsuario,
            Integer idCita,
            String mensaje) {

        if (idUsuario == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario de la notificación es obligatorio.");
        }

        if (idCita == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cita de la notificación es obligatoria.");
        }

        if (mensaje == null || mensaje.trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El mensaje de la notificación es obligatorio.");
        }

        Notificacion notificacion = new Notificacion();

        notificacion.setIdUsuario(idUsuario);
        notificacion.setIdCita(idCita);
        notificacion.setMensaje(mensaje.trim());
        notificacion.setEstado("pendiente");
        notificacion.setFechaEnvio(LocalDateTime.now());

        return notificacionRepository.save(notificacion);
    }

    // CONTAR PENDIENTES
    public long contarPendientes(Integer idUsuario) {

        Usuario usuario = usuarioAutenticado();

        if (!usuario.getId().equals(idUsuario)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes consultar las notificaciones de otro usuario.");
        }

        return notificacionRepository
                .countByIdUsuarioAndEstado(
                        idUsuario,
                        "pendiente");
    }

    // USUARIO AUTENTICADO
    private Usuario usuarioAutenticado() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof Usuario usuario)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No hay un usuario autenticado.");
        }

        return usuario;
    }
}