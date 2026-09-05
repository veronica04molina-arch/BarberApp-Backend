package com.barberapp.barberapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barberapp.barberapp.model.HorarioSemanal;

@Repository
public interface HorarioSemanalRepository
        extends JpaRepository<HorarioSemanal, Integer> {

    List<HorarioSemanal> findByIdBarbero(Integer idBarbero);

    List<HorarioSemanal> findByIdBarberoAndDiaSemana(
            Integer idBarbero,
            String diaSemana
    );

    List<HorarioSemanal> findByIdBarberoAndDiaSemanaAndEstado(
            Integer idBarbero,
            String diaSemana,
            String estado
    );
}