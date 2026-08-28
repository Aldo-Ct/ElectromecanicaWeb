package com.electromecanica.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException excepcion,
                                                      HttpServletRequest solicitud) {
        List<RespuestaError.ErrorCampo> errores = excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> new RespuestaError.ErrorCampo(error.getField(), error.getDefaultMessage()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Revise los datos enviados", solicitud, errores);
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity<RespuestaError> solicitudInvalida(Exception excepcion, HttpServletRequest solicitud) {
        return responder(HttpStatus.BAD_REQUEST, excepcion.getMessage(), solicitud, List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RespuestaError> credencialesInvalidas(BadCredentialsException excepcion,
                                                                 HttpServletRequest solicitud) {
        return responder(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos", solicitud, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> accesoDenegado(AccessDeniedException excepcion,
                                                         HttpServletRequest solicitud) {
        return responder(HttpStatus.FORBIDDEN, "No tiene permiso para realizar esta operación", solicitud, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> integridad(DataIntegrityViolationException excepcion,
                                                      HttpServletRequest solicitud) {
        return responder(HttpStatus.CONFLICT, "La operación entra en conflicto con información existente",
                solicitud, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> inesperado(Exception excepcion, HttpServletRequest solicitud) {
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno al procesar la solicitud",
                solicitud, List.of());
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje,
                                                      HttpServletRequest solicitud,
                                                      List<RespuestaError.ErrorCampo> errores) {
        return ResponseEntity.status(estado).body(new RespuestaError(LocalDateTime.now(), estado.value(),
                mensaje, solicitud.getRequestURI(), errores));
    }
}
