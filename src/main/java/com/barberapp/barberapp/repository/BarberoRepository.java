package com.barberapp.barberapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barberapp.barberapp.model.Barbero;

public interface BarberoRepository extends JpaRepository<Barbero, Integer> {

}