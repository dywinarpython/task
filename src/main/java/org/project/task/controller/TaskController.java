package org.project.task.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.CreateTaskDto;
import org.project.task.dto.request.SetTaskDto;
import org.project.task.dto.response.ListTaskDto;
import org.project.task.dto.response.TaskDto;
import org.project.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
@Tag(name =" Управление задачами")
public class TaskController {
    private final TaskService taskService;





    @Operation(
            summary = "Создания задачи",
            responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createTask(@RequestBody @Valid Mono<CreateTaskDto> createTaskDto,
                                                                @AuthenticationPrincipal Jwt jwt){
        return taskService.saveTask(createTaskDto, jwt)
                .thenReturn(ResponseEntity.status(201).body(Map.of("message", "Задача сохранена")));
    }




    @Operation(
            summary = "Изменение задачи",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PatchMapping
    public Mono<ResponseEntity<Map<String, List<String>>>> setTask(@RequestBody @Valid Mono<SetTaskDto> setTaskDtoMono, @AuthenticationPrincipal Jwt jwt){
        return taskService.setTask(setTaskDtoMono, jwt).map(ResponseEntity::ok);
    }




    @Operation(
            summary = "Удаление задачи",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteTask(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return taskService.delTask(id, jwt)
                .thenReturn(ResponseEntity.status(200).body(Map.of("message", "Задача удалена")));
    }

    @Operation(
            summary = "Получение задач созданных пользователем",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ListTaskDto.class)))
    )
    @GetMapping
    public Mono<ResponseEntity<Map<String, List<TaskDto>>>> deleteTask(@RequestParam String timeZone, @AuthenticationPrincipal Jwt jwt){
        return taskService.getTask(timeZone, jwt).map(tasks -> ResponseEntity.ok(Map.of("tasks", tasks)));
    }
}
