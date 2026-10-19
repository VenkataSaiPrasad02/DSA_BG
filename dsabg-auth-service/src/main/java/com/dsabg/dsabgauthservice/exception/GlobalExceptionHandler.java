
package com.dsabg.dsabgauthservice.exception;

import com.dsabg.dsabgauthservice.service.apiResponse.API_Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<API_Response<String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        return ResponseEntity.badRequest()
                .body(new API_Response<>(
                        "Invalid request format",
                        null
                ));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<API_Response<String>> handleDuplicateResourceException(
            DuplicateResourceException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new API_Response<>(
                        ex.getMessage(),
                        null
                ));
    }
}
