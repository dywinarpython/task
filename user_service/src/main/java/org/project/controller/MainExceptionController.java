package org.project.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class MainExceptionController {

    @ExceptionHandler(NoSuchElementException.class)
    public Mono<ResponseEntity<Map<String, String>>> noSuchElementException(NoSuchElementException e){
        log.warn("Ошикба нахождения элемента: {}", e.getMessage());
        return Mono.just(ResponseEntity.status(404).body(Map.of("warn", e.getMessage())));
    }
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> exception(Exception e){
        log.error("Возникла ошибка, а именно: {}", Arrays.toString(e.getStackTrace()));
        return Mono.just(ResponseEntity.status(404).body(e.getMessage()));
    }
}
