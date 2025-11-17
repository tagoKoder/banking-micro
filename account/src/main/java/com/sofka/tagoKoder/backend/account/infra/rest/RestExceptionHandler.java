package com.sofka.tagoKoder.backend.account.infra.rest;
/*
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.sofka.tagoKoder.backend.account.infra.config.exception.NotFoundException;
import com.sofka.tagoKoder.backend.account.infra.rest.dto.ApiResponse;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  // 400 – JSON mal formado
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      org.springframework.http.converter.HttpMessageNotReadableException ex,
      org.springframework.http.HttpHeaders headers, HttpStatus status,
      org.springframework.web.context.request.WebRequest request) {
    log.warn("Bad JSON: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(ApiResponse.fail("Body of request contains malformed JSON"));
  }

  // 400 – Validación @Valid/@Validated (DTOs)
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpHeaders headers,
      HttpStatus status, org.springframework.web.context.request.WebRequest request) {
    var msg = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"))
        .distinct().reduce((a,b) -> a + "; " + b).orElse("Invalid data");
    return ResponseEntity.badRequest().body(ApiResponse.fail(msg));
  }

  // 400 – Parámetro con tipo incorrecto
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    var field = ex.getName();
    var required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "expected type";
    return ResponseEntity.badRequest().body(ApiResponse.fail("Parameter '" + field + "' must be of type " + required));
  }

  // 400 – Violaciones de @NotNull, @Size, etc. en parámetros (Constraint Validation)
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
    var msg = ex.getConstraintViolations().stream()
        .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
        .distinct().reduce((a,b) -> a + "; " + b).orElse("Invalid data");
    return ResponseEntity.badRequest().body(ApiResponse.fail(msg));
  }

  // 404 – Ruta no existe (requiere propiedad, ver más abajo)
  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
      NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, org.springframework.web.context.request.WebRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Resource not found"));
  }

  // 409 – Conflictos de integridad (únicos, FKs, etc.)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<?>> handleDataIntegrity(DataIntegrityViolationException ex) {
    var root = ExceptionUtils.getRootCauseMessage(ex);
    log.warn("Constraint/DataIntegrity error: {}", root);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail("Data integrity conflict"));
  }

  // Tu 404 de dominio
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(ex.getMessage()));
  }

  // 400 – Peticiones inválidas
  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ApiResponse<?>> handleBadRequest(RuntimeException ex) {
    return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
  }

  // 500 – Fallback
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleAny(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.fail("Unexpected internal error"));
  }
}
*/