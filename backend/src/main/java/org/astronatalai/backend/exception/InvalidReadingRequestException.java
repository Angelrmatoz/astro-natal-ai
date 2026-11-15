package org.astronatalai.backend.exception;

public class InvalidReadingRequestException extends RuntimeException {

    public InvalidReadingRequestException(String message) {
        super(message);
    }
}
