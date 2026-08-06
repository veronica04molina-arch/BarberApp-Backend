package com.barberapp.barberapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barberapp.barberapp.model.Usuario;

/**
 * Repositorio encargado de realizar las operaciones de acceso
 * a la base de datos para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    /**
 * Verifica si existe un usuario con el correo indicado.
 */
boolean existsByEmailIgnoreCase(String email);

Usuario findByEmailIgnoreCase(String email);
}
