package com.barberapp.barberapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.model.Notificacion;
import com.barberapp.barberapp.service.NotificacionService;

@RestController
@RequestMapping("/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(
            NotificacionService notificacionService) {

        this.notificacionService = notificacionService;
    }

    // LISTAR NOTIFICACIONES
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Notificacion>> listarPorUsuario(
            @PathVariable Integer idUsuario) {

        return ResponseEntity.ok(
                notificacionService.listarPorUsuario(idUsuario));
    }

    // MARCAR COMO LEÍDA
    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarComoLeida(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                notificacionService.marcarComoLeida(id));
    }

    // CONTAR PENDIENTES
    @GetMapping("/usuario/{idUsuario}/pendientes")
    public ResponseEntity<Long> contarPendientes(
            @PathVariable Integer idUsuario) {

        return ResponseEntity.ok(
                notificacionService.contarPendientes(idUsuario));
    }
}