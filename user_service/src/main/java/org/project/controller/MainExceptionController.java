package org.project.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class MainExceptionController {
    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleDecodingErrors(DecodingException ex) {
        String message = ex.getMessage();
        log.warn(message, ex);
        return Mono.just(ResponseEntity.badRequest()
                .body(Map.of("warn", "Incorrect data was sent to the server")));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<List<String>>> handleValidationException(WebExchangeBindException ex) {
        log.warn(ex.getMessage(), ex);
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return Mono.just(ResponseEntity.badRequest().body(errors));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Map<String, String>>> noSuchElementException(NoSuchElementException ex){
        log.warn("Ошикба нахождения элемента: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(404).body(Map.of("warn", ex.getMessage())));
    }
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handler(Exception ex){
        log.error("Ошибка сервера: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage())));
    }
}
