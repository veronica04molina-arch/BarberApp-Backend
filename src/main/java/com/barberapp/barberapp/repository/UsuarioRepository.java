package com.barberapp.barberapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barberapp.barberapp.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByEmailIgnoreCase(String email);
    Usuario findByEmailIgnoreCase(String email);
    Optional<Usuario> findByResetToken(String resetToken);
}
