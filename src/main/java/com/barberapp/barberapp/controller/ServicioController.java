package com.barberapp.barberapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.service.ServicioService;

@RestController
@RequestMapping("/servicios")
@CrossOrigin(origins = "*")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public List<Servicio> listarServicios() {
        return servicioService.listarServicios();
    }

    @GetMapping("/activos")
    public List<Servicio> listarServiciosActivos() {
        return servicioService.listarServiciosActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicio> obtenerServicio(
            @PathVariable Integer id) {

        Servicio servicio = servicioService.obtenerServicio(id);

        if (servicio == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(servicio);
    }

    @PostMapping
    public ResponseEntity<?> guardarServicio(
            @RequestBody Servicio servicio) {

        try {

            Servicio nuevoServicio = servicioService.guardarServicio(servicio);

            return ResponseEntity.ok(nuevoServicio);

        } catch (ResponseStatusException e) {

            throw e;

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarServicio(
            @PathVariable Integer id,
            @RequestBody Servicio datosServicio) {

        try {

            Servicio servicioActualizado = servicioService.actualizarServicio(
                    id,
                    datosServicio);

            if (servicioActualizado == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(servicioActualizado);

        } catch (ResponseStatusException e) {

            throw e;

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarServicio(
            @PathVariable Integer id) {

        boolean desactivado = servicioService.desactivarServicio(id);

        if (desactivado) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
