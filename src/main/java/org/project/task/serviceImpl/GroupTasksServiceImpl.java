package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.task.entity.GroupTasks;
import org.project.task.repository.GroupTasksRepository;
import org.project.task.service.GroupTasksService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    public Mono<Boolean> existByTaskIdAndUserID(Long taskId, Jwt jwt) {
        UUID userID = UUID.fromString(jwt.getSubject());
        return groupTasksRepository.existsByTaskIDAndUserIdByGroupTasks(taskId, userID);
    }
}
