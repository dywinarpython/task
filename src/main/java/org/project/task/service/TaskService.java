package org.project.task.service;

import org.project.task.dto.CreateTaskDto;
import org.project.task.dto.SetTaskDto;
import org.project.task.dto.TaskDto;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface TaskService{
    Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDto);
    Mono<Map<String, List<String>>> setTask(Mono<SetTaskDto> setTaskDtoMono);
    Mono<Void> delTask(Long id);
    Mono<List<TaskDto>> getTask();
}
