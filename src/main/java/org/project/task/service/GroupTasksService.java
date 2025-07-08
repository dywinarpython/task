package org.project.task.service;

import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupTasksService {

    Mono<Void> saveTask(Long groupId, Long taskId, UUID userId);
    Mono<Void> saveTask(Long groupId, Long taskId);
    Mono<Boolean> existByTaskIdAndUserID(Long taskId, Jwt jwt);


}
