package org.project.task.service;

import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupUserService {

    Mono<Boolean> validateUserInGroup(Long groupId, UUID userId);
    Flux<Long> getGroupsId(Jwt jwt);
}
