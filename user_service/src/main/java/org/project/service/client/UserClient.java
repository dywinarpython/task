package org.project.service.client;

import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserClient {
    Mono<Void> checkCommonGroups(Jwt jwt, UUID userId);
}
