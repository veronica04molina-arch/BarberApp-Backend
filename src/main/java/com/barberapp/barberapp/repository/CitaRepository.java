package com.barberapp.barberapp.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barberapp.barberapp.model.Cita;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

        // Buscar las citas de un usuario
        List<Cita> findByIdUsuario(Integer idUsuario);

        // Buscar las citas de un barbero
        List<Cita> findByIdBarbero(Integer idBarbero);

        // Verificar si un barbero ya tiene una cita en una fecha y hora
        boolean existsByIdBarberoAndFechaAndHora(
                        Integer idBarbero,
                        LocalDate fecha,
                        LocalTime hora);

        // Buscar citas de un barbero en una fecha específica
        List<Cita> findByIdBarberoAndFecha(
                        Integer idBarbero,
                        LocalDate fecha);

        // Buscar citas de un usuario con un estado específico
        List<Cita> findByIdUsuarioAndEstado(
                        Integer idUsuario,
                        String estado);

        // Buscar citas de un barbero con un estado específico
        List<Cita> findByIdBarberoAndEstado(
                        Integer idBarbero,
                        String estado);

        // Buscar citas de un barbero desde una fecha en adelante
        List<Cita> findByIdBarberoAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
                        Integer idBarbero,
                        LocalDate fecha);

        List<Cita> findByIdUsuarioAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
                        Integer idUsuario,
                        LocalDate fecha);
}