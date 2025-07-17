package org.project.service;

import org.project.dto.response.task.TaskDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupTasksService {

    Mono<Void> saveTask(Jwt jwt, Long groupId, Long taskId, UUID userId);
    Mono<Void> saveTask(Jwt jwt, Long groupId, Long taskId);

    Mono<Long> findGroupIdByTaskID(Long taskId);

    Flux<TaskDto> findTaskForUserWithGroupId(Jwt jwt, Long groupId, Long page);

    Mono<Void> checkingWhetherUserIsPerformingThisTask(Jwt jwt, Long taskId);


}
