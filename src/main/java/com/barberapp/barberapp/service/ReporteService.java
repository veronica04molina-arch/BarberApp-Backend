package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.barberapp.barberapp.dto.ReporteCitasDTO;
import com.barberapp.barberapp.dto.ReporteServicioDTO;
import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;

@Service
public class ReporteService {

    private final CitaRepository citaRepository;
    private final CitaServicioRepository citaServicioRepository;
    private final ServicioRepository servicioRepository;
    private final BarberoRepository barberoRepository;

    public ReporteService(
            CitaRepository citaRepository,
            CitaServicioRepository citaServicioRepository,
            ServicioRepository servicioRepository,
            BarberoRepository barberoRepository) {

        this.citaRepository = citaRepository;
        this.citaServicioRepository = citaServicioRepository;
        this.servicioRepository = servicioRepository;
        this.barberoRepository = barberoRepository;
    }

    // REPORTE GENERAL DE CITAS
    public ReporteCitasDTO obtenerResumenCitas() {

        Integer idBarbero = obtenerBarberoAutenticado().getId();

        long total = citaRepository.countByIdBarbero(idBarbero);

        long pendientes = citaRepository.countByIdBarberoAndEstado(
                idBarbero, "pendiente");

        long confirmadas = citaRepository.countByIdBarberoAndEstado(
                idBarbero, "confirmada");

        long completadas = citaRepository.countByIdBarberoAndEstado(
                idBarbero, "completada");

        long canceladas = citaRepository.countByIdBarberoAndEstado(
                idBarbero, "cancelada");

        return new ReporteCitasDTO(
                total,
                pendientes,
                confirmadas,
                completadas,
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

        Integer idBarbero = obtenerBarberoAutenticado().getId();

        return citaRepository.findByIdBarberoAndFechaBetweenOrderByFechaAscHoraAsc(
                idBarbero,
                fechaInicio,
                fechaFin);
    }

    // REPORTE DE SERVICIOS MÁS SOLICITADOS
    public List<ReporteServicioDTO> obtenerServiciosMasSolicitados() {

        Integer idBarbero = obtenerBarberoAutenticado().getId();

        List<Servicio> servicios = servicioRepository.findByIdBarbero(idBarbero);

        List<ReporteServicioDTO> reporte = new ArrayList<>();

        for (Servicio servicio : servicios) {

            long cantidad = citaServicioRepository
                    .countByIdServicioAndIdBarbero(
                            servicio.getId(),
                            idBarbero);

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

    private Barbero obtenerBarberoAutenticado() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No hay un usuario autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Usuario usuario)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No se pudo obtener el usuario autenticado.");
        }

        if (!"barbero".equalsIgnoreCase(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene rol de barbero.");
        }

        return barberoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un registro de barbero para este usuario."));
    }
}
