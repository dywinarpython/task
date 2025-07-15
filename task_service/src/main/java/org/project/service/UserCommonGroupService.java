package org.project.service;

import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;
public interface UserCommonGroupService {
    Mono<Boolean> hasCommonGroup(Jwt jwt, UUID userId);
}
