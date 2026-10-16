package com.barberapp.barberapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.barberapp.barberapp.model.Sesion;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {

        List<Sesion> findByIdUsuarioOrderByFechaInicioDesc(
                        Integer idUsuario);

        Optional<Sesion> findByTokenAndEstado(
                        String token,
                        String estado);

        Optional<Sesion> findByIdAndIdUsuario(
                        Integer id,
                        Integer idUsuario);

        @Modifying
        @Query("UPDATE Sesion s SET s.estado = 'cerrada' "
                        + "WHERE s.idUsuario = :idUsuario AND LOWER(s.estado) = 'activa'")
        int cerrarSesionesActivasPorUsuario(@Param("idUsuario") Integer idUsuario);
}
