package org.project.task.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.task.dto.CreateTaskDto;
import org.project.task.dto.SetTaskDto;
import org.project.task.dto.TaskDto;
import org.project.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ap1/v1/task")
@RequiredArgsConstructor
@Tag(name =" Управление задачами")
public class TaskController {
    private final TaskService taskService;





    @Operation(
            summary = "Создания задачи",
            responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createTask(@RequestBody @Valid Mono<CreateTaskDto> createTaskDto){
        return taskService.saveTask(createTaskDto)
                .thenReturn(ResponseEntity.status(201).body(Map.of("message", "Задача сохранена")));
    }




    @Operation(
            summary = "Изменение задачи",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PatchMapping
    public Mono<ResponseEntity<Map<String, List<String>>>> setTask(@RequestBody @Valid Mono<SetTaskDto> setTaskDtoMono){
        return taskService.setTask(setTaskDtoMono).map(ResponseEntity::ok);
    }




    @Operation(
            summary = "Удаление задачи",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteTask(@RequestParam Long id){
        return taskService.delTask(id)
                .thenReturn(ResponseEntity.status(200).body(Map.of("message", "Задача удалена")));
    }

    @Operation(
            summary = "Получение задач",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @GetMapping
    public Mono<ResponseEntity<Map<String, List<TaskDto>>>> deleteTask(){
        return taskService.getTask().map(tasks -> ResponseEntity.ok(Map.of("tasks", tasks)));
    }
}
