package org.project.task.service;

import org.project.task.dto.request.task.CreateTaskDto;
import org.project.task.dto.request.task.CreateTaskWithUserDto;
import org.project.task.dto.request.task.SetTaskDto;
import org.project.task.dto.response.task.TaskDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TaskService{
    Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDto, Jwt jwt);
    Mono<Void> saveTaskByUserID(Mono<CreateTaskWithUserDto> createTaskWithUserDtoMono, Jwt jwt);
    Mono<Void> setTask(Mono<SetTaskDto> setTaskDtoMono, Jwt jwt);
    Mono<Void> delTask(Long id, Jwt jwt);
    Mono<List<TaskDto>> getTasks(String timeZone, Jwt jwt, Long groupId);
    Mono<List<TaskDto>> getTasksForUser(String timeZone, Jwt jwt, Long groupId);
    Mono<Void> completeTask(Jwt jwt, Long taskId);

}
