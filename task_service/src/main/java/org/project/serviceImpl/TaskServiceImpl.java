package org.project.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.task.CreateTaskDto;
import org.project.dto.request.task.CreateTaskWithUserDto;
import org.project.dto.request.task.SetTaskDto;
import org.project.dto.response.task.TaskDto;
import org.project.dto.response.task.TaskWithUserDto;
import org.project.entity.Task;
import org.project.mapper.task.MapperTask;
import org.project.repository.TaskRepository;
import org.project.service.GroupTasksService;
import org.project.service.GroupUserService;
import org.project.service.KafkaService;
import org.project.service.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;


import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    @Value("${pageable.size}")
    private Long size;


    private final TransactionalOperator transactionalOperator;

    private final TaskRepository taskRepository;


    private final GroupUserService groupUserService;


    private final GroupTasksService groupTasksService;

    private final MapperTask mapperTask;

    private final KafkaService kafkaService;



    @Override
    public Mono<List<TaskWithUserDto>> getTasks(String timeZone, Jwt jwt, Long groupId, Long page) {
        return groupUserService.verifyAdminAccess(groupId, jwt).then(
                taskRepository.findByGroupId(groupId, size, page*size ).map(task -> mapperTask.taskToTaskWithUserDto(task, timeZone)).collectList());
    }

    @Override
    public Mono<List<TaskDto>> getTasksForUser(String timeZone, Jwt jwt, Long groupId, Long page) {
        return groupUserService.checkUserInGroup(groupId, UUID.fromString(jwt.getSubject()))
                .then(groupTasksService.findTaskForUserWithGroupId(jwt, groupId, page).collectList().map(task -> mapperTask.taskListDtoTTaskListDto(task, timeZone)));
    }

    @Override
    public Mono<Void> completeTask(Jwt jwt, Long taskId) {
        return groupTasksService.checkingWhetherUserIsPerformingThisTask(jwt, taskId).then(
                        taskRepository.updateFields(mapperTask.createCompleteTask(), Task.class, "id", taskId));
    }

    @Override
    public Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDtoMono, Jwt jwt) {
        return createTaskDtoMono
                .flatMap(dto ->  groupUserService.verifyAdminAccess(dto.groupId(), jwt).thenReturn(dto))
                .flatMap(dto ->
                    taskRepository.save(mapperTask.taskDtoToTask(dto)).flatMap(task -> groupTasksService.saveTask(jwt, dto.groupId(), task.getId())
                    )
                .then(kafkaService.sendMessageToAllUserInGroup(dto.groupId()))
                .as(transactionalOperator::transactional)
                );
    }

    @Override
    public Mono<Void> saveTaskByUserID(Mono<CreateTaskWithUserDto> createTaskWithUserDtoMono, Jwt jwt) {
        return createTaskWithUserDtoMono
                .flatMap(dto ->  groupUserService.verifyAdminAccess(dto.groupId(), jwt)
                        .thenReturn(dto))
                .flatMap(dto -> groupUserService.checkUserInGroup(dto.groupId(), dto.userID()).thenReturn(dto))
                .flatMap(dto ->
                        taskRepository.save(mapperTask.taskDtoToTask(dto))
                                .flatMap(task -> groupTasksService.saveTask(jwt, dto.groupId(), task.getId(), dto.userID()))
                                .then(kafkaService.sendMessageToNotifications(dto.userID(), "Вам назначенна личная задача, проверьте список задач."))
                                .as(transactionalOperator::transactional)
                )
                .then();
    }


    @Override
    public Mono<Void> setTask(Mono<SetTaskDto> setTaskDtoMono, Jwt jwt) {
        return setTaskDtoMono
                .flatMap(setTaskDto ->
                     groupTasksService.findGroupIdByTaskID(setTaskDto.id()).flatMap( groupId ->
                    groupUserService.verifyAdminAccess(groupId, jwt).thenReturn(mapperTask.createUpdateTaskFields(setTaskDto))
                            .flatMap(sqlIdentifierObjectMap -> taskRepository.updateFields(sqlIdentifierObjectMap, Task.class, "id", setTaskDto.id()))
                ));
    }
    @Override
    public Mono<Void> delTask(Long id, Jwt jwt) {
        return groupTasksService.findGroupIdByTaskID(id)
                .flatMap(groupId ->
                        groupUserService.verifyAdminAccess(groupId, jwt)
                                .then(taskRepository.deleteByIdReturning(id).next())
                )
                .switchIfEmpty(Mono.error(new NoSuchElementException("Task with id: " + id + " is not found")))
                .then();
    }

    @Override
    public Mono<Void> delAllTaskByGroupId(Long groupId) {
        return taskRepository.deleteTaskByGroupId(groupId);
    }
}
