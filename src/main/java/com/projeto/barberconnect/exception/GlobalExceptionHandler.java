package com.projeto.barberconnect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            EmailAlreadyExistsException.class
    )
    public ResponseEntity<Map<String, String>>
    handleEmailAlreadyExists(
            EmailAlreadyExistsException ex
    ) {

        Map<String, String> error =
                new HashMap<>();

        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }
    @ExceptionHandler(
            InvalidCredentialsException.class
    )
    public ResponseEntity<Map<String, String>>
    handleInvalidCredentials(
            InvalidCredentialsException ex
    ) {

        Map<String, String> error =
                new HashMap<>();

        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>
    handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors =
                new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(
            InvalidRefreshTokenException.class
    )
    public ResponseEntity<Map<String,String>> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex
    ){
        Map<String, String> errors =
                new HashMap<>();
        errors.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errors);
    }

    @ExceptionHandler(
            InvalidOtpException.class
    )
    public ResponseEntity<Map<String,String>> handleInvalidOtp(InvalidOtpException ex){
        Map<String, String> errors = new HashMap<>();

        errors.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
    }
    @ExceptionHandler(
            EmailNotVerifiedException.class
    )
    public ResponseEntity<Map<String,String>> handleEmailNotVerified(EmailNotVerifiedException ex){
        Map<String, String> errors =
                new HashMap<>();
        errors.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errors);
    }

    @ExceptionHandler(
            BusinessException.class
    )
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex) {
        Map<String, String> errors =
                new HashMap<>();
        errors.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
    }

    @ExceptionHandler(
            InvalidCnpjException.class
    )
    public ResponseEntity<Map<String, String>> handleInvalidCnpj(InvalidCnpjException ex) {
        Map<String, String> errors =
                new HashMap<>();
        errors.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(
            CnpjAlreadyExistsException.class
    )
    public ResponseEntity<Map<String, String>> handleCnpjAlreadyExists(CnpjAlreadyExistsException ex) {
        Map<String, String> errors =
                new HashMap<>();
        errors.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
    }

    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    public ResponseEntity<Map<String,String>> handleResourceNotFound(ResourceNotFoundException ex){
        Map<String, String> errors =
                new HashMap<>();
        errors.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);

    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> errors =
                new HashMap<>();
        errors.put("message", "Invalid value for " + ex.getName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);
    }

}
