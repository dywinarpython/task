package org.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.project.dto.UserDto;
import org.project.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Запрос информации о пользователе"
    )
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserDto>> getUserById(@PathVariable UUID userId, @AuthenticationPrincipal Jwt jwt){
        return userService.findUserByUserID(userId, jwt).map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Запрос информации о пользователе"
    )
    @GetMapping
    public Mono<ResponseEntity<UserDto>> getUser(@AuthenticationPrincipal Jwt jwt){
        return userService.findUser(jwt).map(ResponseEntity::ok);
    }

}
