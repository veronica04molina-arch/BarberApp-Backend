package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.dto.ReporteCitasDTO;
import com.barberapp.barberapp.dto.ReporteServicioDTO;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@Service
public class ReporteService {

    private final CitaRepository citaRepository;
    private final CitaServicioRepository citaServicioRepository;
    private final ServicioRepository servicioRepository;

    public ReporteService(
            CitaRepository citaRepository,
            CitaServicioRepository citaServicioRepository,
            ServicioRepository servicioRepository) {

        this.citaRepository = citaRepository;
        this.citaServicioRepository = citaServicioRepository;
        this.servicioRepository = servicioRepository;
    }

    // REPORTE GENERAL DE CITAS
    public ReporteCitasDTO obtenerResumenCitas() {

        long total = citaRepository.count();

        long pendientes = citaRepository.countByEstado("pendiente");

        long canceladas = citaRepository.countByEstado("cancelada");

        return new ReporteCitasDTO(
                total,
                pendientes,
                canceladas);
    }

    // REPORTE DE CITAS POR RANGO DE FECHAS
    public List<Cita> obtenerCitasPorRango(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException(
                    "Las fechas son obligatorias.");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final.");
        }

        return citaRepository.findByFechaBetweenOrderByFechaAscHoraAsc(
                fechaInicio,
                fechaFin);
    }

    // REPORTE DE SERVICIOS MÁS SOLICITADOS
    public List<ReporteServicioDTO> obtenerServiciosMasSolicitados() {

        List<Servicio> servicios = servicioRepository.findAll();

        List<ReporteServicioDTO> reporte = new ArrayList<>();

        for (Servicio servicio : servicios) {

            long cantidad = citaServicioRepository.countByIdServicio(
                    servicio.getId());

            reporte.add(
                    new ReporteServicioDTO(
                            servicio.getNombre(),
                            cantidad));
        }

        reporte.sort(
                (a, b) -> Long.compare(
                        b.getCantidad(),
                        a.getCantidad()));

        return reporte;
    }
}