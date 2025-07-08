package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.task.CreateTaskDto;
import org.project.task.dto.request.task.CreateTaskWithUserDto;
import org.project.task.dto.request.task.SetTaskDto;
import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.Task;
import org.project.task.mapper.task.MapperTask;
import org.project.task.repository.TaskRepository;
import org.project.task.service.GroupService;
import org.project.task.service.GroupTasksService;
import org.project.task.service.GroupUserService;
import org.project.task.service.TaskService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;


import java.util.*;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TransactionalOperator transactionalOperator;

    private final TaskRepository taskRepository;


    private final GroupUserService groupUserService;

    private final GroupService groupService;

    private final GroupTasksService groupTasksService;

    private final MapperTask mapperTask;



    @Override
    public Mono<List<TaskDto>> getTasks(String timeZone, Jwt jwt, Long groupId) {
        return groupService.verifyUserAccess(groupId, jwt).then(
                taskRepository.findByGroupID(groupId).map(task -> mapperTask.taskToTaskDto(task, timeZone)).collectList());
    }




    @Override
    public Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDtoMono, Jwt jwt) {
        return createTaskDtoMono
                .flatMap(dto -> groupService.verifyUserAccess(dto.groupId(), jwt).thenReturn(dto))
                .flatMap(dto ->
                    taskRepository.save(mapperTask.taskDtoToTask(dto)).flatMap(task -> groupTasksService.saveTask(dto.groupId(), task.getId())
                    ).as(transactionalOperator::transactional).then()
                );
    }

    @Override
    public Mono<Void> saveTaskByUserID(Mono<CreateTaskWithUserDto> createTaskWithUserDtoMono, Jwt jwt) {
        return createTaskWithUserDtoMono
                .flatMap(dto ->  groupService.verifyUserAccess(dto.groupId(), jwt)
                        .thenReturn(dto))
                .flatMap(dto -> groupUserService.validateUserInGroup(dto.groupId(), dto.userID()).thenReturn(dto))
                .flatMap(dto ->
                        taskRepository.save(mapperTask.taskDtoToTask(dto))
                                .flatMap(task -> groupTasksService.saveTask(dto.groupId(), task.getId(), dto.userID()))
                                .as(transactionalOperator::transactional)
                )
                .then();
    }


    @Override
    public Mono<Void> setTask(Mono<SetTaskDto> setTaskDtoMono, Jwt jwt) {
        return setTaskDtoMono
                .flatMap(setTaskDto ->
                    verifyUserAccess(setTaskDto.id(), jwt).thenReturn(mapperTask.createUpdateTaskFields(setTaskDto))
                            .flatMap(sqlIdentifierObjectMap -> taskRepository.updateFields(sqlIdentifierObjectMap, Task.class, "id", setTaskDto.id()))
                );
    }
    @Override
    public Mono<Void> delTask(Long id, Jwt jwt) {
        return verifyUserAccess(id, jwt)
                .then(taskRepository.deleteById(id));
    }


    public Mono<Void> verifyUserAccess(Long taskId, Jwt jwt) {
        return groupTasksService.existByTaskIdAndUserID(taskId, jwt).flatMap(bl -> {
            if (!bl) {
                return taskRepository.existsById(taskId).flatMap(ex -> {
                            if (!ex) {
                                return Mono.error(new NoSuchElementException("Элемент с id: " + taskId + " не найден"));
                            }
                            return Mono.error(new AccessDeniedException("У вас нет доступа работать с данной задачей"));
                        }
                );
            }
            return Mono.empty();
        });
    }
}
