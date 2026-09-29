package com.barberapp.barberapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barberapp.barberapp.model.CitaServicio;

@Repository
public interface CitaServicioRepository extends JpaRepository<CitaServicio, Integer> {

    List<CitaServicio> findByIdCita(Integer idCita);

    List<CitaServicio> findByIdServicio(Integer idServicio);

    boolean existsByIdCitaAndIdServicio(
            Integer idCita,
            Integer idServicio);

    long countByIdServicio(Integer idServicio);
}