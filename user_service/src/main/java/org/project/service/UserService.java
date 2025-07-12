package org.project.service;

import org.project.dto.UserDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {
    Mono<UserDto> findUserByUserID(UUID userId, Jwt jwt);
}
