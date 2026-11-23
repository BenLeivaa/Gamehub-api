package com.gamehub.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo centralizado de errores: cada excepción se convierte en una respuesta JSON
 * con el código HTTP correcto (404, 400, 401, 403).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(ResourceNotFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> reglaDeNegocio(BusinessException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> noAutenticado(AuthenticationException ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> sinPermiso(AccessDeniedException ex) {
        return respuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Datos inválidos");
        body.put("detalles", errores);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> peticionMalFormada(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "La petición está mal formada o tiene valores inválidos");
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
