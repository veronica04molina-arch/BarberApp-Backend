package com.barberapp.barberapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.dto.AgendaCitaDTO;
import com.barberapp.barberapp.service.AgendaService;

@RestController
@RequestMapping("/agenda")
@CrossOrigin(origins = "*")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping("/barbero/{idBarbero}")
    public ResponseEntity<?> obtenerAgendaBarbero(
            @PathVariable Integer idBarbero) {

        try {

            List<AgendaCitaDTO> citas = agendaService
                    .obtenerAgendaBarbero(idBarbero);

            return ResponseEntity.ok(citas);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(404)
                    .body(e.getMessage());
        }
    }
}