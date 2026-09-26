package com.barberapp.barberapp.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.service.DisponibilidadService;

@RestController
@RequestMapping("/disponibilidad")
@CrossOrigin(origins = "*")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(
            DisponibilidadService disponibilidadService) {

        this.disponibilidadService = disponibilidadService;
    }

    // Consultar horarios disponibles de un barbero
    @GetMapping("/barbero/{idBarbero}/fecha/{fecha}")
    public ResponseEntity<List<LocalTime>> obtenerHorariosDisponibles(
            @PathVariable Integer idBarbero,
            @PathVariable LocalDate fecha,
            @RequestParam int duracion) {

        List<LocalTime> horarios =
                disponibilidadService.obtenerHorariosDisponibles(
                        idBarbero,
                        fecha,
                        duracion);

        return ResponseEntity.ok(horarios);
    }
} 
