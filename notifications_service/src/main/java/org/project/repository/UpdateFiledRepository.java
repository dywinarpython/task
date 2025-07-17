package org.project.repository;

import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;

@FunctionalInterface
public interface UpdateFiledRepository {

    Mono<Void> updateField(List<String> notificationsId, Jwt jwt);
}
