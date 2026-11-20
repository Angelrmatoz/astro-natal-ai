package org.astronatalai.backend.exception;

public class InvalidProfileRequestException extends RuntimeException {

    public InvalidProfileRequestException(String message) {
        super(message);
    }
}
