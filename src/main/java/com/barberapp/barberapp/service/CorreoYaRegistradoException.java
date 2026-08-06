package com.barberapp.barberapp.service;

/** Indica que se intentó crear una cuenta con un correo ya registrado. */
public class CorreoYaRegistradoException extends RuntimeException {
    public CorreoYaRegistradoException() {
        super("Ya existe una cuenta registrada con ese correo.");
    }
}
