package com.barberapp.barberapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.service.CitaService;

@RestController
@RequestMapping("/citas")
@CrossOrigin(origins = "*")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    // Listar todas las citas
    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // Buscar una cita por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {

        return citaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Buscar citas de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Cita>> listarPorUsuario(
            @PathVariable Integer idUsuario) {

        return ResponseEntity.ok(
                citaService.listarPorUsuario(idUsuario));
    }

    // Buscar citas de un barbero
    @GetMapping("/barbero/{idBarbero}")
    public ResponseEntity<List<Cita>> listarPorBarbero(
            @PathVariable Integer idBarbero) {

        return ResponseEntity.ok(
                citaService.listarPorBarbero(idBarbero));
    }

    // Buscar citas de un barbero en una fecha específica
    @GetMapping("/barbero/{idBarbero}/fecha/{fecha}")
    public ResponseEntity<List<Cita>> listarPorBarberoYFecha(
            @PathVariable Integer idBarbero,
            @PathVariable LocalDate fecha) {

        return ResponseEntity.ok(
                citaService.listarPorBarberoYFecha(
                        idBarbero,
                        fecha));
    }

    // Crear una cita
    @PostMapping
    public ResponseEntity<?> guardarCita(
            @RequestBody Cita cita) {

        try {

            Cita nuevaCita = citaService.guardarCita(cita);

            return ResponseEntity.ok(nuevaCita);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // Actualizar una cita
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCita(
            @PathVariable Integer id,
            @RequestBody Cita datosCita) {

        try {

            Cita citaActualizada = citaService.actualizarCita(
                    id,
                    datosCita);

            if (citaActualizada == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(citaActualizada);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // Cancelar una cita
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(
            @PathVariable Integer id,
            @RequestParam Integer idUsuario) {

        try {
            boolean cancelada = citaService.cancelarCita(id, idUsuario);

            if (!cancelada) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(
                    "La cita fue cancelada correctamente.");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}