package com.barberapp.barberapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.barberapp.barberapp.auth.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(
                        JwtAuthenticationFilter jwtAuthenticationFilter) {

                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http

                                .csrf(csrf -> csrf.disable())

                                .cors(cors -> {
                                })

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response, authException) -> response
                                                                .sendError(HttpServletResponse.SC_UNAUTHORIZED))
                                                .accessDeniedHandler((request, response, accessDeniedException) -> response
                                                                .sendError(HttpServletResponse.SC_FORBIDDEN)))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers("/auth/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/usuarios/registrar")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.OPTIONS,
                                                                "/**")
                                                .permitAll()

                                                // SERVICIOS
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/servicios",
                                                                "/servicios/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/servicios",
                                                                "/servicios/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/servicios/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/servicios",
                                                                "/servicios/**")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                // DISPONIBILIDAD
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/disponibilidad",
                                                                "/disponibilidad/**")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                .requestMatchers("/agenda/barbero/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/barbero")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/barbero/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/barbero",
                                                                "/api/barbero/**")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/horarios-semanales")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/horarios-semanales/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/horarios-semanales/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/horarios-semanales",
                                                                "/horarios-semanales/**")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                // EXCEPCIONES DE HORARIO
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/excepciones-horario")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/excepciones-horario/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/excepciones-horario/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/excepciones-horario",
                                                                "/excepciones-horario/**")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                // CITAS
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/citas")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/citas/*")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/citas/usuario/**")
                                                .hasRole("CLIENTE")

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/citas/barbero/**")
                                                .hasRole("BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/citas")
                                                .hasRole("CLIENTE")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/citas/*/cancelar")
                                                .hasRole("CLIENTE")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/citas/**")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                // NOTIFICACIONES
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/notificaciones/usuario/**")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/notificaciones/*/leer")
                                                .hasAnyRole("CLIENTE", "BARBERO")

                                                .anyRequest().authenticated())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
