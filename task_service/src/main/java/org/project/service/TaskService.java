package org.project.service;

import org.project.dto.request.task.CreateTaskDto;
import org.project.dto.request.task.CreateTaskWithUserDto;
import org.project.dto.request.task.SetTaskDto;
import org.project.dto.response.task.TaskDto;
import org.project.dto.response.task.TaskWithUserDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TaskService{
    Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDto, Jwt jwt);
    Mono<Void> saveTaskByUserID(Mono<CreateTaskWithUserDto> createTaskWithUserDtoMono, Jwt jwt);
    Mono<Void> setTask(Mono<SetTaskDto> setTaskDtoMono, Jwt jwt);
    Mono<Void> delTask(Long id, Jwt jwt);
    Mono<Void> delAllTaskByGroupId(Long groupId);
    Mono<List<TaskWithUserDto>> getTasks(String timeZone, Jwt jwt, Long groupId, Long page);
    Mono<List<TaskDto>> getTasksForUser(String timeZone, Jwt jwt, Long groupId, Long page);
    Mono<Void> completeTask(Jwt jwt, Long taskId);

}
