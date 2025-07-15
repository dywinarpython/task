package org.project.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.task.TaskDto;
import org.project.entity.GroupTasks;
import org.project.repository.GroupTasksRepository;
import org.project.service.GroupTasksService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupTasksServiceImpl implements GroupTasksService {

    @Value("${pageable.size}")
    private Long size;

    private final GroupTasksRepository groupTasksRepository;



    @Override
    public Mono<Void> saveTask(Jwt jwt, Long groupId, Long taskId, UUID userId) {
        return groupTasksRepository.save(GroupTasks
                .builder()
                .groupId(groupId)
                .taskId(taskId)
                .userId(userId)
                .assignBy(UUID.fromString(jwt.getSubject()))
                .build()
                )
        .then();
    }
    @Override
    public Mono<Void> saveTask(Jwt jwt, Long groupId, Long taskId) {
        return groupTasksRepository.save(GroupTasks
                .builder()
                .groupId(groupId)
                .taskId(taskId)
                .assignBy(UUID.fromString(jwt.getSubject()))
                .build())
        .then();
    }

    @Override
    public Mono<Long> findGroupIdByTaskID(Long taskId) {
        return groupTasksRepository.findGroupIdByTaskId(taskId);
    }

    @Override
    public Flux<TaskDto> findTaskForUserWithGroupId(Jwt jwt, Long groupId, Long page) {
        return groupTasksRepository.findTaskForUserWithGroupID(UUID.fromString(jwt.getSubject()), groupId, size, page * size);
    }

    @Override
    public Mono<Void> checkingWhetherUserIsPerformingThisTask(Jwt jwt, Long taskId) {
        return groupTasksRepository.checkingWhetherUserIsPerformingThisTask(UUID.fromString(jwt.getSubject()), taskId)
                .flatMap(hasAccess -> {
                    if (hasAccess) {
                        return Mono.empty();
                    }
                    return groupTasksRepository.existsByTaskId(taskId)
                            .flatMap(taskExists -> {
                                if (taskExists) {
                                    return Mono.error(new AccessDeniedException("Access is denied"));
                                } else {
                                    return Mono.error(new NoSuchElementException("Task with id: " + taskId + " not found"));
                                }
                            });
                });
    }


}
