package com.barberapp.barberapp.controller;

import java.time.LocalDate;
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

import com.barberapp.barberapp.model.ExcepcionHorario;
import com.barberapp.barberapp.service.ExcepcionHorarioService;

@RestController
@RequestMapping("/excepciones-horario")
@CrossOrigin(origins = "*")
public class ExcepcionHorarioController {

        private final ExcepcionHorarioService excepcionHorarioService;

        public ExcepcionHorarioController(
                        ExcepcionHorarioService excepcionHorarioService) {

                this.excepcionHorarioService = excepcionHorarioService;
        }

        // LISTAR TODAS LAS EXCEPCIONES
        @GetMapping
        public ResponseEntity<List<ExcepcionHorario>> listarExcepciones() {

                return ResponseEntity.ok(
                                excepcionHorarioService.listarExcepciones());
        }

        // BUSCAR EXCEPCIÓN POR ID
        @GetMapping("/{id}")
        public ResponseEntity<?> buscarPorId(
                        @PathVariable Integer id) {

                return excepcionHorarioService
                                .buscarPorId(id)
                                .map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        // LISTAR EXCEPCIONES DE UN BARBERO
        @GetMapping("/barbero/{idBarbero}")
        public ResponseEntity<?> listarPorBarbero(
                        @PathVariable Integer idBarbero) {

                try {

                        return ResponseEntity.ok(
                                        excepcionHorarioService
                                                        .listarPorBarbero(idBarbero));

                } catch (RuntimeException e) {

                        return ResponseEntity.badRequest()
                                        .body(e.getMessage());
                }
        }

        // LISTAR EXCEPCIONES DE UN BARBERO Y FECHA
        @GetMapping("/barbero/{idBarbero}/fecha/{fecha}")
        public ResponseEntity<?> listarPorBarberoYFecha(
                        @PathVariable Integer idBarbero,
                        @PathVariable LocalDate fecha) {

                try {

                        return ResponseEntity.ok(
                                        excepcionHorarioService
                                                        .listarPorBarberoYFecha(
                                                                        idBarbero,
                                                                        fecha));

                } catch (RuntimeException e) {

                        return ResponseEntity.badRequest()
                                        .body(e.getMessage());
                }
        }

        // CREAR EXCEPCIÓN
        @PostMapping
        public ResponseEntity<?> guardarExcepcion(
                        @RequestBody ExcepcionHorario excepcion) {

                try {

                        ExcepcionHorario nuevaExcepcion = excepcionHorarioService
                                        .guardarExcepcion(excepcion);

                        return ResponseEntity.ok(nuevaExcepcion);

                } catch (RuntimeException e) {

                        return ResponseEntity.badRequest()
                                        .body(e.getMessage());
                }
        }

        // ACTUALIZAR EXCEPCIÓN
        @PutMapping("/{id}")
        public ResponseEntity<?> actualizarExcepcion(
                        @PathVariable Integer id,
                        @RequestBody ExcepcionHorario datosExcepcion) {

                try {

                        ExcepcionHorario excepcionActualizada = excepcionHorarioService
                                        .actualizarExcepcion(
                                                        id,
                                                        datosExcepcion);

                        if (excepcionActualizada == null) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        return ResponseEntity.ok(
                                        excepcionActualizada);

                } catch (RuntimeException e) {

                        return ResponseEntity.badRequest()
                                        .body(e.getMessage());
                }
        }

        // ELIMINAR EXCEPCIÓN
        @DeleteMapping("/{id}")
        public ResponseEntity<?> eliminarExcepcion(
                        @PathVariable Integer id) {

                boolean eliminado = excepcionHorarioService
                                .eliminarExcepcion(id);

                if (!eliminado) {

                        return ResponseEntity
                                        .notFound()
                                        .build();
                }

                return ResponseEntity.ok(
                                "La excepción de horario fue eliminada correctamente.");
        }
}