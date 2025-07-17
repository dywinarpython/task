package org.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.project.dto.response.task.ListTaskDto;
import org.project.dto.response.task.TaskDto;
import org.project.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Tag(name = "Управление задачами пользователями")
@RestController
@RequestMapping("/api/v1/user/task")
@RequiredArgsConstructor
public class UserTaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Получение задач назначенных пользователю",
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
                    content = @Content(schema = @Schema(implementation = ListTaskDto.class))
            )
    )
    @GetMapping
    public Mono<ResponseEntity<Map<String, List<TaskDto>>>> getTasks(
            @RequestParam Long groupId,
            @RequestParam String timeZone,
            @RequestParam Long page,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return taskService.getTasksForUser(timeZone, jwt, groupId, page)
                .map(tasks -> ResponseEntity.ok(Map.of("tasks", tasks)));
    }

    @Operation(
            summary = "Завершение задачи",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PatchMapping("/{taskId}")
    public Mono<ResponseEntity<Map<String, String>>> completeTask(@PathVariable("taskId") Long taskId, @AuthenticationPrincipal Jwt jwt){
        return taskService.completeTask(jwt, taskId).thenReturn(ResponseEntity.ok(Map.of("message", "Задача завершена")));
    }
}
