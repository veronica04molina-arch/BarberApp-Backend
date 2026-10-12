package com.barberapp.barberapp.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.barberapp.barberapp.model.Barbero;

public interface BarberoRepository extends JpaRepository<Barbero, Integer> {
    boolean existsByUsuarioId(Integer idUsuario);

    Optional<Barbero> findByUsuarioId(Integer idUsuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select barbero from Barbero barbero where barbero.id = :idBarbero")
    Optional<Barbero> findByIdForUpdate(@Param("idBarbero") Integer idBarbero);
}
