package com.hotel.exception;

import com.hotel.dto.ApiError;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HotelException.class)
    public ResponseEntity<Object> handleHotel(HotelException exception, WebRequest request) {
        return error(exception.status(), exception.code(), exception.getMessage(), Map.of(), new HttpHeaders(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(status, "VALIDATION_ERROR", "Revisa los datos del formulario.", errors, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = switch (status.value()) {
            case 400 -> "La solicitud no es válida. Verifica campos, fechas, UUID y valores permitidos.";
            case 404 -> "El recurso solicitado no existe.";
            case 405 -> "El método HTTP no está permitido para este recurso.";
            case 406 -> "El formato de respuesta solicitado no está disponible.";
            case 415 -> "El contenido debe enviarse como application/json.";
            default -> "No fue posible procesar la solicitud.";
        };
        return error(status, "HTTP_" + status.value(), message, Map.of(), headers, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception exception, WebRequest request) {
        LOG.error("Unexpected request failure", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocurrió un error inesperado. Intenta nuevamente.", Map.of(), new HttpHeaders(), request);
    }

    private ResponseEntity<Object> error(HttpStatusCode status, String code, String message, Map<String, String> errors,
            HttpHeaders headers, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        return new ResponseEntity<>(new ApiError(Instant.now(), status.value(), code, message, path, errors), headers, status);
    }
}
