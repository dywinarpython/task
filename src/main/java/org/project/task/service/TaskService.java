package org.project.task.service;

import org.project.task.dto.request.CreateTaskDto;
import org.project.task.dto.request.SetTaskDto;
import org.project.task.dto.response.TaskDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface TaskService{
    Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDto, Jwt jwt);
    Mono<Map<String, List<String>>> setTask(Mono<SetTaskDto> setTaskDtoMono, Jwt jwt);
    Mono<Void> delTask(Long id, Jwt jwt);
    Mono<List<TaskDto>> getTask(String timeZone, Jwt jwt);
}
