package com.barberapp.barberapp;

/*
 * Framework seleccionado: Spring Boot.
 * Se eligió porque facilita el desarrollo de aplicaciones web mediante una
 * arquitectura en capas, permite integrar PostgreSQL y desarrollar APIs
 * REST de forma rápida y organizada para el proyecto BarberApp.
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class BarberappApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarberappApplication.class, args);
	}

}
