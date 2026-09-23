package com.barberapp.barberapp.auth;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.barberapp.barberapp.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtService jwtService;
        private final UsuarioRepository usuarioRepository;

        public JwtAuthenticationFilter(
                        JwtService jwtService,
                        UsuarioRepository usuarioRepository) {

                this.jwtService = jwtService;
                this.usuarioRepository = usuarioRepository;
        }

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain)
                        throws ServletException, IOException {

                String authorizationHeader = request.getHeader("Authorization");

                if (authorizationHeader == null
                                || !authorizationHeader.startsWith("Bearer ")) {

                        filterChain.doFilter(request, response);
                        return;
                }

                String token = authorizationHeader.substring(7);

                if (!jwtService.validarToken(token)) {

                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                }

                String email = jwtService.obtenerEmail(token);

                if (email != null
                                && SecurityContextHolder
                                                .getContext()
                                                .getAuthentication() == null) {

                        var usuario = usuarioRepository
                                        .findByEmailIgnoreCase(email);

                        if (usuario != null) {

                                String rol = usuario.getRol();

                                SimpleGrantedAuthority autoridad = new SimpleGrantedAuthority(
                                                "ROLE_" + rol.toUpperCase());

                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                usuario,
                                                null,
                                                Collections.singletonList(autoridad));

                                authentication.setDetails(
                                                new WebAuthenticationDetailsSource()
                                                                .buildDetails(request));

                                SecurityContextHolder
                                                .getContext()
                                                .setAuthentication(authentication);
                        }
                }

                filterChain.doFilter(request, response);
        }
}