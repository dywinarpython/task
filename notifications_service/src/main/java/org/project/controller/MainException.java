package org.project.controller;


import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ValidationException;
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
@ControllerAdvice
@Hidden
@Slf4j
public class MainException {

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleDecodingErrors(DecodingException ex) {
        String message = ex.getMessage();
        log.warn(message);
        return Mono.just(ResponseEntity.badRequest()
                .body(Map.of("warn", "Incorrect data was sent to the server")));
    }

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

}
