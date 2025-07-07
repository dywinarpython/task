package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.task.CreateTaskDto;
import org.project.task.dto.request.task.SetTaskDto;
import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.Task;
import org.project.task.mapper.task.MapperTask;
import org.project.task.repository.TaskRepository;
import org.project.task.service.GroupService;
import org.project.task.service.TaskService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final GroupService groupService;

    private final MapperTask mapperTask;


    @Override
    public Mono<List<TaskDto>> getTasks(String timeZone, Jwt jwt, Long groupId) {
        return taskRepository.findByGroupId(groupId).map(task -> mapperTask.taskToTaskDto(task, timeZone)).collectList(); }

    @Override
    public Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDtoMono, Jwt jwt) {
        return createTaskDtoMono
                .flatMap(dto -> groupService.verifyUserAccess(dto.groupId(), jwt).thenReturn(dto))
                .flatMap(dto -> {
                    Task task = mapperTask.taskDtoToTask(dto);
                    return taskRepository.save(task).then();
                });
    }

    @Override
    public Mono<Map<String, List<String>>> setTask(Mono<SetTaskDto> setTaskDtoMono, Jwt jwt) {
        return setTaskDtoMono.flatMap(setTaskDto ->
                verifyUserAccess(setTaskDto.id(), jwt).then(updateTaskFields(setTaskDto)));

    }

    @Override
    public Mono<Void> delTask(Long id, Jwt jwt) {
        return verifyUserAccess(id, jwt)
                .then(taskRepository.deleteById(id));
    }

    public Mono<Void> verifyUserAccess(Long taskId, Jwt jwt) {
        UUID userID = UUID.fromString(jwt.getSubject());
        return taskRepository.existsByIdAndUserIdWithTableGroup(taskId, userID).flatMap(bl -> {
            if (!bl) {
                return taskRepository.existsById(taskId).flatMap(ex -> {
                            if (!ex) {
                                return Mono.error(new NoSuchElementException("Элемент с id: " + taskId + " не найден"));
                            }
                            return Mono.error(new AccessDeniedException("У вас нет доступа изменять данную задачу"));
                        }
                );
            }
            return Mono.empty();
        });
    }

    private Mono<Map<String, List<String>>> updateTaskFields(SetTaskDto setTaskDto){
        return taskRepository.findById(setTaskDto.id()).flatMap(task -> {
            List<String> updateFields = new ArrayList<>();
            if (setTaskDto.deadLine() != null) {
                task.setDeadLine(LocalDateTime.now().plus(Duration.ofMillis(setTaskDto.deadLine())));
                updateFields.add("deadLine");
            }
            if(setTaskDto.description() != null) {
                task.setDescription(setTaskDto.description());
                updateFields.add("description");
            }
            if(setTaskDto.name() != null) {
                task.setName(setTaskDto.name());
                updateFields.add("name");
            }
            if(setTaskDto.status() != null) {
                task.setStatus(setTaskDto.status());
                updateFields.add("status");
            }
            if(updateFields.isEmpty()){
                return Mono.empty();
            }
            return taskRepository.save(task).thenReturn(
                    Map.of("update_fields", updateFields));
        });
    }
}
