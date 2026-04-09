package com.marketplace.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// Intercepta excepciones de todos los controllers
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja errores de validación (@Valid falló)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        // Recopilamos todos los errores de validación en un mapa
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(fieldName, message);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", "Datos de entrada inválidos");
        response.put("errors", fieldErrors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    // Maneja credenciales incorrectas (login fallido)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex
    ) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }

    // Maneja ResponseStatusException (los errores que lanzamos nosotros)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex
    ) {
        return buildErrorResponse(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason()
        );
    }

    // Maneja cualquier otra excepción no capturada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor"
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }
}
