package org.astronatalai.backend.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("El email ya está registrado: " + email);
    }

    public EmailAlreadyUsedException(String email, Throwable cause) {
        super("El email ya está registrado: " + email, cause);
    }
}
