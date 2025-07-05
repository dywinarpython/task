package org.project.task.controller;


import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
@Hidden
@Slf4j
public class MainException {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<List<String>>> handleValidationException(WebExchangeBindException ex) {
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return Mono.just(ResponseEntity.badRequest().body(errors));
    }
    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(ValidationException e){
        log.warn("Возникла ошибка валидации, а именно: {}", e.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(Map.of("warn", e.getMessage())));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(NoSuchElementException e){
        log.warn(e.getMessage());
        return Mono.just(ResponseEntity.status(404).body(Map.of("warn", e.getMessage())));
    }
    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(AuthenticationException e){
        log.error(e.getMessage());
        return Mono.just(ResponseEntity.status(403).body(Map.of("error", e.getMessage())));
    }
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(Exception e){
        log.error("Возникла ошибка, а именно: {}", e.getMessage());
        return Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage())));
    }
}
