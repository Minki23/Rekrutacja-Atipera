package com.example.rekrutacja_atipera.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GithubApiException.class)
    public ResponseEntity<Map<String, Object>> handleGithubApiException(GithubApiException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ex.getStatus().value());
        body.put("message", ex.getBody());

        return ResponseEntity.status(ex.getStatus()).body(body);
    }
}
