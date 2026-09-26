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
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.dto.CitaRequest;
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

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return citaService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    // Buscar citas de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Integer idUsuario) {
        try {
            return ResponseEntity.ok(
                    citaService.listarCitasCliente(idUsuario));
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Buscar citas de un barbero
    @GetMapping("/barbero/{idBarbero}")
    public ResponseEntity<?> listarPorBarbero(
            @PathVariable Integer idBarbero) {
        try {
            return ResponseEntity.ok(
                    citaService.listarPorBarbero(idBarbero));
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Buscar citas de un barbero en una fecha
    @GetMapping("/barbero/{idBarbero}/fecha/{fecha}")
    public ResponseEntity<?> listarPorBarberoYFecha(
            @PathVariable Integer idBarbero,
            @PathVariable LocalDate fecha) {
        try {
            return ResponseEntity.ok(
                    citaService.listarPorBarberoYFecha(
                            idBarbero,
                            fecha));
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> guardarCita(
            @RequestBody CitaRequest request) {
        try {
            Cita nuevaCita = citaService.guardarCita(request);
            return ResponseEntity.ok(nuevaCita);

        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

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

        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(
            @PathVariable Integer id,
            @RequestParam Integer idUsuario) {
        try {
            boolean cancelada = citaService.cancelarCita(
                    id,
                    idUsuario);

            if (!cancelada) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(
                    "La cita fue cancelada correctamente.");

        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{idUsuario}/proxima")
    public ResponseEntity<?> obtenerProximaCita(
            @PathVariable Integer idUsuario) {

        try {

            return citaService.obtenerProximaCita(idUsuario)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }
}