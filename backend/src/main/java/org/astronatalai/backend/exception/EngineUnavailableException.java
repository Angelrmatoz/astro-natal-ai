package org.astronatalai.backend.exception;

public class EngineUnavailableException extends RuntimeException {

    public EngineUnavailableException(String message) {
        super(message);
    }

    public EngineUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
