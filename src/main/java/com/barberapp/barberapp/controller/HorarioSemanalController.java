package com.barberapp.barberapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.model.HorarioSemanal;
import com.barberapp.barberapp.service.HorarioSemanalService;

@RestController
@RequestMapping("/horarios-semanales")
@CrossOrigin(origins = "*")
public class HorarioSemanalController {

    private final HorarioSemanalService horarioSemanalService;

    public HorarioSemanalController(
            HorarioSemanalService horarioSemanalService) {

        this.horarioSemanalService = horarioSemanalService;
    }

    // Listar todos los horarios
    @GetMapping
    public ResponseEntity<List<HorarioSemanal>> listarHorarios() {

        return ResponseEntity.ok(
                horarioSemanalService.listarHorarios()
        );
    }

    // Buscar horario por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id) {

        return horarioSemanalService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    // Listar horarios de un barbero
    @GetMapping("/barbero/{idBarbero}")
    public ResponseEntity<?> listarPorBarbero(
            @PathVariable Integer idBarbero) {

        try {

            return ResponseEntity.ok(
                    horarioSemanalService
                            .listarPorBarbero(idBarbero)
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // Listar horarios de un barbero para un día
    @GetMapping("/barbero/{idBarbero}/dia/{diaSemana}")
    public ResponseEntity<?> listarPorBarberoYDia(
            @PathVariable Integer idBarbero,
            @PathVariable String diaSemana) {

        try {

            return ResponseEntity.ok(
                    horarioSemanalService
                            .listarPorBarberoYDia(
                                    idBarbero,
                                    diaSemana
                            )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // Listar únicamente horarios disponibles
    @GetMapping("/barbero/{idBarbero}/dia/{diaSemana}/disponibles")
    public ResponseEntity<?> listarDisponibles(
            @PathVariable Integer idBarbero,
            @PathVariable String diaSemana) {

        try {

            return ResponseEntity.ok(
                    horarioSemanalService
                            .listarDisponibles(
                                    idBarbero,
                                    diaSemana
                            )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // Crear horario
    @PostMapping
    public ResponseEntity<?> guardarHorario(
            @RequestBody HorarioSemanal horario) {

        try {

            HorarioSemanal nuevoHorario =
                    horarioSemanalService
                            .guardarHorario(horario);

            return ResponseEntity.ok(nuevoHorario);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // Actualizar horario
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarHorario(
            @PathVariable Integer id,
            @RequestBody HorarioSemanal datosHorario) {

        try {

            HorarioSemanal horarioActualizado =
                    horarioSemanalService
                            .actualizarHorario(
                                    id,
                                    datosHorario
                            );

            if (horarioActualizado == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            return ResponseEntity.ok(
                    horarioActualizado
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    // Eliminar horario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarHorario(
            @PathVariable Integer id) {

        boolean eliminado =
                horarioSemanalService
                        .eliminarHorario(id);

        if (!eliminado) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                "El horario fue eliminado correctamente."
        );
    }
}