package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.GroupTasks;
import org.project.task.repository.GroupTasksRepository;
import org.project.task.service.GroupTasksService;
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

    private final GroupTasksRepository groupTasksRepository;



    @Override
    public Mono<Void> saveTask(Long groupId, Long taskId, UUID userId) {
        return groupTasksRepository.save(GroupTasks.builder().groupId(groupId).taskId(taskId).userId(userId).build()).then();
    }
    @Override
    public Mono<Void> saveTask(Long groupId, Long taskId) {
        return groupTasksRepository.save(GroupTasks.builder().groupId(groupId).taskId(taskId).build()).then();
    }

    @Override
    public Mono<Long> findGroupIdByTaskID(Long taskId) {
        return groupTasksRepository.findGroupIdByTaskId(taskId);
    }

    @Override
    public Flux<TaskDto> findTaskForUserWithGroupId(Jwt jwt, Long groupId) {
        return groupTasksRepository.findTaskForUserWithGroupID(UUID.fromString(jwt.getSubject()), groupId);
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
