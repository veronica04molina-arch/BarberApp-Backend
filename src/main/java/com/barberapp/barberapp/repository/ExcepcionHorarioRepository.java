package com.barberapp.barberapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barberapp.barberapp.model.ExcepcionHorario;

public interface ExcepcionHorarioRepository
        extends JpaRepository<ExcepcionHorario, Integer> {

    // Buscar excepciones de un barbero
    List<ExcepcionHorario> findByIdBarbero(Integer idBarbero);

    // Buscar excepciones de un barbero en una fecha
    List<ExcepcionHorario> findByIdBarberoAndFecha(
            Integer idBarbero,
            LocalDate fecha
    );
}