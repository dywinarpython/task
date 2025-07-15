package org.project.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.dto.request.task.CreateTaskDto;
import org.project.dto.request.task.CreateTaskWithUserDto;
import org.project.dto.request.task.SetTaskDto;
import org.project.dto.response.task.ListTaskWithUserDto;
import org.project.dto.response.task.TaskWithUserDto;
import org.project.service.TaskService;
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
            summary = "Создания задачи для всех участников группы",
            responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createTask(@RequestBody @Valid Mono<CreateTaskDto> createTaskDto,
                                                                @AuthenticationPrincipal Jwt jwt){
        return taskService.saveTask(createTaskDto, jwt)
                .thenReturn(ResponseEntity.status(201).body(Map.of("message", "Задача сохранена")));
    }

    @Operation(
            summary = "Создания задачи для участника группы",
            responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PostMapping("/user")
    public Mono<ResponseEntity<Map<String, String>>> createTaskFoUser(@RequestBody @Valid Mono<CreateTaskWithUserDto> createTaskWithUserDtoMono,
                                                                @AuthenticationPrincipal Jwt jwt){
        return taskService.saveTaskByUserID(createTaskWithUserDtoMono, jwt)
                .thenReturn(ResponseEntity.status(201).body(Map.of("message", "Задача сохранена")));
    }


    @Operation(
            summary = "Изменение задачи",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PatchMapping
    public Mono<ResponseEntity<Map<String, String>>> setTask(@RequestBody @Valid Mono<SetTaskDto> setTaskDtoMono, @AuthenticationPrincipal Jwt jwt){
        return taskService.setTask(setTaskDtoMono, jwt).thenReturn(ResponseEntity.ok(Map.of("message", "Задача измененна")));
    }




    @Operation(
            summary = "Удаление задачи",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{taskId}")
    public Mono<ResponseEntity<Map<String, String>>> deleteTask(@PathVariable Long taskId, @AuthenticationPrincipal Jwt jwt){
        return taskService.delTask(taskId, jwt)
                .thenReturn(ResponseEntity.status(200).body(Map.of("message", "Задача удалена")));
    }

    @Operation(
            summary = "Получение задач определенной группы",
            parameters = {
                    @Parameter(
                            name = "groupId",
                            description = "Идентификатор группы",
                            required = true,
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "timeZone",
                            description = "Часовой пояс клиента (например, Europe/Moscow)",
                            required = true,
                            in = ParameterIn.QUERY,
                            schema = @Schema(type = "string", defaultValue = "Europe/Moscow", example = "Europe/Moscow")
                    )
            },
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Список задач",
                    content = @Content(schema = @Schema(implementation = ListTaskWithUserDto.class))
            )
    )
    @GetMapping
    public Mono<ResponseEntity<Map<String, List<TaskWithUserDto>>>> getTasks(
            @RequestParam Long groupId,
            @RequestParam String timeZone,
            @RequestParam Long page,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return taskService.getTasks(timeZone, jwt, groupId, page)
                .map(tasks -> ResponseEntity.ok(Map.of("tasks", tasks)));
    }

}
