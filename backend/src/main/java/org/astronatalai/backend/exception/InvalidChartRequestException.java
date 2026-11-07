package org.astronatalai.backend.exception;

public class InvalidChartRequestException extends RuntimeException {

    public InvalidChartRequestException(String message) {
        super(message);
    }
}
