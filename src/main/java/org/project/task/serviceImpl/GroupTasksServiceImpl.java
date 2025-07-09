package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.GroupTasks;
import org.project.task.repository.GroupTasksRepository;
import org.project.task.service.GroupTasksService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.security.sasl.AuthenticationException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupTasksServiceImpl implements GroupTasksService {

    private final GroupTasksRepository groupTasksRepository;


    @Override
    public Mono<Void> saveTask(Long groupId, Long taskId, UUID userId) {
        return groupTasksRepository.save(GroupTasks.builder().groupId(groupId).taskID(taskId).userID(userId).build()).then();
    }
    @Override
    public Mono<Void> saveTask(Long groupId, Long taskId) {
        return groupTasksRepository.save(GroupTasks.builder().groupId(groupId).taskID(taskId).build()).then();
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
    public Mono<Void> checkingWhetherUserIsPerformingThisTask(Jwt jwt, Long groupId, Long taskId) {
        return groupTasksRepository.checkingWhetherUserIsPerformingThisTask(UUID.fromString(jwt.getSubject()), groupId, taskId).flatMap(bl -> {
                    if (!bl) {
                        return Mono.error(new AuthenticationException("Access is denied"));
                    }
                    return Mono.empty();
                }
                );
    }


}
