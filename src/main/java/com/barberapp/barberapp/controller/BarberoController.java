package com.barberapp.barberapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.service.BarberoService;

@RestController
@RequestMapping("/api/barbero")
public class BarberoController {

    private final BarberoService barberoService;

    public BarberoController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    // Obtener todos los barberos
    @GetMapping
    public List<Barbero> listarBarberos() {
        return barberoService.listarBarberos();
    }

    // Obtener un barbero por ID
    @GetMapping("/{id}")
    public ResponseEntity<Barbero> buscarPorId(@PathVariable Integer id) {

        Optional<Barbero> barbero = barberoService.buscarPorId(id);

        if (barbero.isPresent()) {
            return ResponseEntity.ok(barbero.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Registrar un barbero
    @PostMapping
public ResponseEntity<?> guardarBarbero(@RequestBody Barbero barbero) {

    try {

        Barbero nuevoBarbero = barberoService.guardarBarbero(barbero);

        return ResponseEntity.ok(nuevoBarbero);

    } catch (RuntimeException e) {

        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }
}

    // Actualizar un barbero
    @PutMapping("/{id}")
    public ResponseEntity<Barbero> actualizarBarbero(
            @PathVariable Integer id,
            @RequestBody Barbero datosBarbero) {

        Barbero barberoActualizado =
                barberoService.actualizarBarbero(id, datosBarbero);

        if (barberoActualizado != null) {
            return ResponseEntity.ok(barberoActualizado);
        }

        return ResponseEntity.notFound().build();
    }

    // Desactivar un barbero
    @PutMapping("/{id}/desactivar")
public ResponseEntity<Void> desactivarBarbero(@PathVariable Integer id) {

    boolean desactivado = barberoService.desactivarBarbero(id);

    if (desactivado) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
}
}