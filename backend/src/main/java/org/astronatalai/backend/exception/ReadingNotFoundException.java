package org.astronatalai.backend.exception;

public class ReadingNotFoundException extends RuntimeException {

    public ReadingNotFoundException() {
        super("Todavía no hay una lectura para este periodo");
    }
}
