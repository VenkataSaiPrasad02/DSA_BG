
package com.dsabg.dsabgauthservice.exception;

import com.dsabg.dsabgauthservice.service.apiResponse.API_Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<API_Response<String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = "Invalid request format";

        if (ex.getBindingResult() != null
                && ex.getBindingResult().getFieldError() != null
                && ex.getBindingResult().getFieldError().getDefaultMessage() != null) {

            message = ex.getBindingResult()
                    .getFieldError()
                    .getDefaultMessage();
        }

        return ResponseEntity.badRequest()
                .body(new API_Response<>(message, null));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<API_Response<String>> handleDuplicateResourceException(
            DuplicateResourceException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new API_Response<>(ex.getMessage(), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<API_Response<String>> handleBadCredentials(
            BadCredentialsException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new API_Response<>(ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<API_Response<String>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest()
                .body(new API_Response<>(ex.getMessage(), null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<API_Response<String>> handleRuntime(
            RuntimeException ex) {

        log.error("Unexpected runtime exception", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new API_Response<>(
                        "An internal error occurred", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<API_Response<String>> handleException(
            Exception ex) {

        log.error("Unexpected exception", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new API_Response<>(
                        "An internal error occurred", null));
    }
}
