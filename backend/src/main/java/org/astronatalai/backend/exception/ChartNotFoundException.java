package org.astronatalai.backend.exception;

public class ChartNotFoundException extends RuntimeException {

    public ChartNotFoundException() {
        super("No hay una carta natal guardada para este usuario");
    }
}
