package com.barberapp.barberapp.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.model.Sesion;
import com.barberapp.barberapp.repository.SesionRepository;

@Service
public class SesionService {

    private final SesionRepository sesionRepository;

    public SesionService(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    // REGISTRAR SESIÓN
    public Sesion registrarSesion(
            Integer idUsuario,
            String token) {

        Sesion sesion = new Sesion();

        sesion.setIdUsuario(idUsuario);
        sesion.setToken(hashToken(token));
        sesion.setFechaInicio(LocalDateTime.now());

        // El JWT actual tiene una duración de 24 horas
        sesion.setFechaExpiracion(
                LocalDateTime.now().plusHours(24));

        sesion.setEstado("activa");

        return sesionRepository.save(sesion);
    }

    // LISTAR SESIONES DEL USUARIO
    public List<Sesion> listarSesiones(Integer idUsuario) {

        return sesionRepository
                .findByIdUsuarioOrderByFechaInicioDesc(idUsuario);
    }

    // CERRAR UNA SESIÓN
    public Sesion cerrarSesion(
            Integer idSesion,
            Integer idUsuario) {

        Sesion sesion = sesionRepository
                .findByIdAndIdUsuario(
                        idSesion,
                        idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La sesión no existe."));

        if ("cerrada".equalsIgnoreCase(sesion.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La sesión ya se encuentra cerrada.");
        }

        sesion.setEstado("cerrada");

        return sesionRepository.save(sesion);
    }

    // VALIDAR TOKEN ACTIVO
    public boolean sesionActiva(String token) {

        return sesionRepository
                .findByTokenAndEstado(hashToken(token), "activa")
                .isPresent();
    }

    public void cerrarSesionPorToken(String token) {

        Sesion sesion = sesionRepository
                .findByTokenAndEstado(hashToken(token), "activa")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La sesión activa no existe."));

        sesion.setEstado("cerrada");

        sesionRepository.save(sesion);
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No está disponible SHA-256.", e);
        }
    }
}
