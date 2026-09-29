package com.barberapp.barberapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.dto.ReporteCitasDTO;
import com.barberapp.barberapp.dto.ReporteServicioDTO;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.service.ReporteService;

@RestController
@RequestMapping("/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // REPORTE GENERAL DE CITAS
    @GetMapping("/citas/resumen")
    public ResponseEntity<ReporteCitasDTO> obtenerResumenCitas() {

        return ResponseEntity.ok(
                reporteService.obtenerResumenCitas());
    }

    // REPORTE DE CITAS POR RANGO DE FECHAS
    @GetMapping("/citas")
    public ResponseEntity<?> obtenerCitasPorRango(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        try {

            List<Cita> citas = reporteService.obtenerCitasPorRango(
                    fechaInicio,
                    fechaFin);

            return ResponseEntity.ok(citas);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // REPORTE DE SERVICIOS MÁS SOLICITADOS
    @GetMapping("/servicios")
    public ResponseEntity<List<ReporteServicioDTO>> obtenerServiciosMasSolicitados() {

        return ResponseEntity.ok(
                reporteService.obtenerServiciosMasSolicitados());
    }
}