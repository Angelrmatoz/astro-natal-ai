package org.astronatalai.backend.exception;

import java.util.Map;
import java.util.Objects;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    ProblemDetail emailAlreadyUsed(EmailAlreadyUsedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "El email ya está registrado");
        problem.setTitle("Email ya registrado");
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail badCredentials(BadCredentialsException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        problem.setTitle("Credenciales inválidas");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "La petición contiene datos inválidos");
        problem.setTitle("Validación fallida");
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Objects.requireNonNullElse(error.getDefaultMessage(), "valor inválido")))
                .toList());
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail unreadable(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es JSON válido");
        problem.setTitle("Petición inválida");
        return problem;
    }

    @ExceptionHandler(InvalidChartRequestException.class)
    ProblemDetail invalidChart(InvalidChartRequestException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Datos de nacimiento inválidos");
        return problem;
    }

    @ExceptionHandler(ChartNotFoundException.class)
    ProblemDetail chartNotFound(ChartNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Carta natal no encontrada");
        return problem;
    }

    @ExceptionHandler(GeocodingException.class)
    ProblemDetail geocoding(GeocodingException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problem.setTitle("No se pudo resolver la ubicación");
        return problem;
    }

    @ExceptionHandler(EngineUnavailableException.class)
    ProblemDetail engineUnavailable(EngineUnavailableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "El motor astrológico no está disponible");
        problem.setTitle("Servicio no disponible");
        return problem;
    }
}
