package com.barberapp.barberapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.service.CitaServicioService;

@RestController
@RequestMapping("/cita-servicios")
public class CitaServicioController {

        private final CitaServicioService citaServicioService;

        public CitaServicioController(
                        CitaServicioService citaServicioService) {
                this.citaServicioService = citaServicioService;
        }

        // Listar todas las relaciones cita-servicio
        @GetMapping
        public ResponseEntity<List<CitaServicio>> listarCitaServicios() {
                return ResponseEntity.ok(
                                citaServicioService.listarCitaServicios());
        }

        // Buscar una relación por ID
        @GetMapping("/{id}")
        public ResponseEntity<?> buscarPorId(
                        @PathVariable Integer id) {

                try {
                        return citaServicioService.buscarPorId(id)
                                        .map(ResponseEntity::ok)
                                        .orElseGet(() -> ResponseEntity.notFound().build());
                } catch (ResponseStatusException e) {
                        return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
                }
        }

        // Listar servicios de una cita
        @GetMapping("/cita/{idCita}")
        public ResponseEntity<?> listarPorCita(
                        @PathVariable Integer idCita) {

                try {
                        return ResponseEntity.ok(citaServicioService.listarPorCita(idCita));
                } catch (ResponseStatusException e) {
                        return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
                }
        }

        // Listar citas que utilizan un servicio
        @GetMapping("/servicio/{idServicio}")
        public ResponseEntity<?> listarPorServicio(
                        @PathVariable Integer idServicio) {

                try {
                        return ResponseEntity.ok(citaServicioService.listarPorServicio(idServicio));
                } catch (ResponseStatusException e) {
                        return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
                }
        }

        // Asociar un servicio a una cita
        @PostMapping
        public ResponseEntity<?> guardarCitaServicio(
                        @RequestBody CitaServicio citaServicio) {

                try {

                        CitaServicio nuevaRelacion = citaServicioService.guardarCitaServicio(
                                        citaServicio);

                        return ResponseEntity.ok(nuevaRelacion);

                } catch (ResponseStatusException e) {

                        return ResponseEntity.status(e.getStatusCode()).body(e.getReason());

                } catch (RuntimeException e) {

                        return ResponseEntity.badRequest()
                                        .body(e.getMessage());
                }
        }

        // Eliminar un servicio de una cita
        @DeleteMapping("/{id}")
        public ResponseEntity<?> eliminarCitaServicio(
                        @PathVariable Integer id) {

                try {
                        boolean eliminado = citaServicioService.eliminarCitaServicio(id);

                        if (!eliminado) {
                                return ResponseEntity.notFound().build();
                        }

                        return ResponseEntity.ok("El servicio fue eliminado de la cita correctamente.");
                } catch (ResponseStatusException e) {
                        return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
                }
        }
}
