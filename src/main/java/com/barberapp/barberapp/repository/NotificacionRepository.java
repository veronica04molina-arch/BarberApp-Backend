package com.barberapp.barberapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.barberapp.barberapp.model.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

        List<Notificacion> findByIdUsuarioOrderByFechaEnvioDesc(
                        Integer idUsuario);

        List<Notificacion> findByIdUsuarioAndEstadoOrderByFechaEnvioDesc(
                        Integer idUsuario,
                        String estado);

        long countByIdUsuarioAndEstado(
                        Integer idUsuario,
                        String estado);

        @Modifying
        @Query("""
                        UPDATE Notificacion n
                        SET n.estado = 'leida', n.fechaLectura = :ahora
                        WHERE n.idUsuario = :idUsuario AND n.estado = 'pendiente'
                        """)
        int marcarTodasComoLeidas(@Param("idUsuario") Integer idUsuario,
                        @Param("ahora") LocalDateTime ahora);
}