package org.project.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.project.task.config.TestSecurityConfig;
import org.project.task.dto.CreateTaskDto;
import org.project.task.dto.SetTaskDto;
import org.project.task.dto.TaskDto;
import org.project.task.entity.Task;
import org.project.task.mapper.task.MapperTask;
import org.project.task.repository.TaskReposiroty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class)
class TaskApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskReposiroty taskReposiroty;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MapperTask mapperTask;

    CreateTaskDto dto = new CreateTaskDto("Задача 1", "Описать структуру БД", 124000L, "В планах работы", "Europe/Moscow");


    @Test
    @DisplayName("Проверка POST /api/v1/task")
    void testCreateTask() throws JsonProcessingException {

        webTestClient.post()
                .uri("/api/v1/task")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(dto))
                .exchange()
                .expectStatus().isCreated();


        Task savedTask = taskReposiroty.findByName("Задача 1").blockFirst(Duration.ofSeconds(5));
        assertNotNull(savedTask, "Задача не была сохранена");
        assertEquals("Задача 1", savedTask.getName());
    }
    @Test
    @DisplayName("Проверка PATH /api/v1/task")
    void testSetTask() throws JsonProcessingException {

        taskReposiroty.save(mapperTask.taskDtoToTask(dto, ZoneId.of(dto.zoneID()))).block();
        Task task = taskReposiroty.findByName(dto.name()).blockFirst();
        assertNotNull(task, "Ошибка сохранения задачи");
        SetTaskDto setTaskDto = new SetTaskDto(task.getId(), "Задача 1 изменена", null, 100435L, null, "Europe/Moscow");
        webTestClient.patch()
                .uri("/api/v1/task")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(setTaskDto))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(objectMapper.writeValueAsString(Map.of("update", List.of("name", "dead_line", "create_time"))));


        Task savedTask = taskReposiroty.findByName(setTaskDto.name()).blockFirst();
        assertNotNull(savedTask, "Задача не была изменена");
        assertEquals(setTaskDto.name(), savedTask.getName());
    }

    @Test
    @DisplayName("Проверка GET /api/v1/task")
    void testGetTask() {
        taskReposiroty.save(mapperTask.taskDtoToTask(dto, ZoneId.of(dto.zoneID()))).block();
        Task task = taskReposiroty.findByName(dto.name()).blockFirst();
        assertNotNull(task, "Ошибка сохранения задачи");
        Flux<TaskDto> taskDtoFlux = webTestClient.get()
                .uri("/api/v1/task")
                .exchange()
                .expectStatus().isOk()
                .returnResult(TaskDto.class)
                .getResponseBody();

        StepVerifier.create(taskDtoFlux)
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }
    @Test
    @DisplayName("Проверка DELETE /api/v1/task")
    void testDeleteTask() {
        taskReposiroty.save(mapperTask.taskDtoToTask(dto, ZoneId.of(dto.zoneID()))).block();
        Task task = taskReposiroty.findByName(dto.name()).blockFirst();
        assertNotNull(task, "Ошибка сохранения задачи");
        webTestClient.delete()
                .uri("/api/v1/task/" + task.getId())
                .exchange()
                .expectStatus().isOk();
        Task savedTask = taskReposiroty.findById(1L).block(Duration.ofSeconds(5));
        assertNull(savedTask, "Задача не была удалена");
    }


}
