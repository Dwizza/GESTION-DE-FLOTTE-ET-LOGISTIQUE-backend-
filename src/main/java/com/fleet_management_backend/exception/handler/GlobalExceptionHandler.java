package com.fleet_management_backend.exception.handler;

import com.fleet_management_backend.dto.error.ApiError;
import com.fleet_management_backend.exception.BadRequestException;
import com.fleet_management_backend.exception.ConflictException;
import com.fleet_management_backend.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = new LinkedHashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message("Validation failed")
                                .path(request.getRequestURI())
                                .validationErrors(errors)
                                .build();

                return ResponseEntity.badRequest().body(body);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiError> handleConstraintViolation(
                        ConstraintViolationException ex,
                        HttpServletRequest request) {
                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message("Les données saisies ne respectent pas les contraintes requises.")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.badRequest().body(body);
        }

        // ========= Database / Hibernate errors =========
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> handleDataIntegrity(
                        DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                log.error("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());

                String userMessage = "Opération impossible : une contrainte de base de données a été violée.";

                // Detect common cases and give friendlier messages
                String rootMsg = ex.getMostSpecificCause().getMessage().toLowerCase();
                if (rootMsg.contains("duplicate") || rootMsg.contains("unique") || rootMsg.contains("already exists")) {
                        userMessage = "Un enregistrement avec ces informations existe déjà. Veuillez vérifier les données saisies.";
                } else if (rootMsg.contains("foreign key") || rootMsg.contains("fk_") || rootMsg.contains("référence")) {
                        userMessage = "Impossible de supprimer ou modifier cet élément car il est lié à d'autres données.";
                } else if (rootMsg.contains("not-null") || rootMsg.contains("null value")) {
                        userMessage = "Un champ obligatoire est manquant. Veuillez remplir tous les champs requis.";
                }

                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.CONFLICT.value())
                                .error(HttpStatus.CONFLICT.getReasonPhrase())
                                .message(userMessage)
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        // ========= Malformed JSON =========
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleBadJson(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                log.error("Malformed request body at {}: {}", request.getRequestURI(), ex.getMessage());

                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message("Le format de la requête est invalide. Veuillez vérifier les données envoyées.")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.badRequest().body(body);
        }

        // ========= 403 =========
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(
                        AccessDeniedException ex,
                        HttpServletRequest request) {
                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                                .message("Accès refusé. Vous n'avez pas les permissions nécessaires.")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }

        // ========= 404 =========
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiError> handleNotFound(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {
                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        // ========= 409 =========
        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ApiError> handleConflict(
                        ConflictException ex,
                        HttpServletRequest request) {
                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.CONFLICT.value())
                                .error(HttpStatus.CONFLICT.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        // ========= 400 =========
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiError> handleBadRequest(
                        BadRequestException ex,
                        HttpServletRequest request) {
                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // ========= 500 (fallback) =========
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGeneric(
                        Exception ex,
                        HttpServletRequest request) {
                // Log the full error for debugging — but NEVER expose it to the frontend
                log.error("Unexpected error at {}: {} - {}", request.getRequestURI(), ex.getClass().getName(), ex.getMessage(), ex);

                ApiError body = ApiError.builder()
                                .timestamp(Instant.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                                .message("Une erreur interne est survenue. Veuillez réessayer ou contacter l'administrateur.")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
}
