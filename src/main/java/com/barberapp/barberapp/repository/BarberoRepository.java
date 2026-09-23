package com.barberapp.barberapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barberapp.barberapp.model.Barbero;

public interface BarberoRepository extends JpaRepository<Barbero, Integer> {
    boolean existsByUsuarioId(Integer idUsuario);

    Optional<Barbero> findByUsuarioId(Integer idUsuario);
}