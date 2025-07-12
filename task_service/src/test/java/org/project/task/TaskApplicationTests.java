package org.project.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.project.task.config.TestSecurityConfig;
import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.dto.request.group.SetUserRole;
import org.project.task.dto.request.task.CreateTaskDto;
import org.project.task.dto.request.task.CreateTaskWithUserDto;
import org.project.task.dto.request.task.SetTaskDto;
import org.project.task.dto.response.group.GroupDto;
import org.project.task.dto.response.group.UserDto;
import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.Group;
import org.project.task.entity.GroupTasks;
import org.project.task.entity.GroupUsers;
import org.project.task.entity.Task;
import org.project.task.mapper.task.MapperTask;
import org.project.task.repository.*;
import org.project.task.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
@EnableAutoConfiguration(exclude = {
        JpaRepositoriesAutoConfiguration.class
})
@Import(TestSecurityConfig.class)
class TaskApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupTasksRepository groupTasksRepository;

    @Autowired
    private GroupUsersRepository groupUsersRepository;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private MapperTask mapperTask;




    Jwt jwt = Jwt.withTokenValue("dummy-token")
            .header("alg", "none")
            .claim("sub", "123e4567-e89b-12d3-a456-426614174000")
            .build();

    private CreateTaskDto dto;
    private Task task;
    private Group group;

/*   Creating an issue when linking to a group,
   because in the set and delete methods, the search is based on the group_task entity.
 */
    @BeforeEach
    void setUp() {
        CreateGroupDto createGroupDto = new CreateGroupDto("Компания", "Без описания");
        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .post()
                .uri("/api/v1/group")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createGroupDto)
                .exchange()
                .expectStatus().isCreated();
        Optional<Group> optionalGroup = Optional.ofNullable(groupRepository.findByName("Компания").blockFirst());
        assertTrue(optionalGroup.isPresent());
        group = optionalGroup.get();
        dto = new CreateTaskDto(group.getId() ,"Задача 1", "Описать структуру БД", 12400000L, "В планах работы");
        task = taskRepository.save(mapperTask.taskDtoToTask(dto)).block();
        assertNotNull(task, "Ошибка сохранения задачи");
        GroupTasks groupTasks = new GroupTasks();
        groupTasks.setTaskId(task.getId());
        groupTasks.setGroupId(dto.groupId());
        groupTasksRepository.save(groupTasks).block();
    }


    @Test
    @DisplayName("Проверка POST /api/v1/task")
    void testCreateTask(){

        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .post()
                .uri("/api/v1/task")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated();


        Task savedTask = taskRepository.findByName("Задача 1").blockFirst(Duration.ofSeconds(5));
        assertNotNull(savedTask, "Задача не была сохранена");
        assertEquals("Задача 1", savedTask.getName());
    }

    @Test
    @DisplayName("Проверка POST /api/v1/task/user")
    void testCreateTaskForUser() {

        UUID userID = UUID.randomUUID();
        CreateTaskWithUserDto createTaskWithUserDto = new CreateTaskWithUserDto(userID, group.getId(), "Задача для пользователя", dto.description(), dto.deadLine(), dto.status());
        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .post()
                .uri("/api/v1/task")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createTaskWithUserDto)
                .exchange()
                .expectStatus().isCreated();


        Task savedTask = taskRepository.findByName(createTaskWithUserDto.name()).blockFirst(Duration.ofSeconds(5));
        assertNotNull(savedTask, "Задача не была сохранена");
        assertEquals(createTaskWithUserDto.name(), savedTask.getName());
    }
    @Test
    @DisplayName("Проверка PATH /api/v1/task")
    void testSetTask() {
        taskRepository.save(mapperTask.taskDtoToTask(dto)).block();
        Task task = taskRepository.findByName(dto.name()).blockFirst();
        assertNotNull(task, "Ошибка сохранения задачи");
        SetTaskDto setTaskDto = new SetTaskDto(task.getId(), "Задача 1 изменена", null, 10043599L, null);
        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .patch()
                .uri("/api/v1/task")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(setTaskDto)
                .exchange()
                .expectStatus().isOk();


        Task savedTask = taskRepository.findByName(setTaskDto.name()).blockFirst();
        assertNotNull(savedTask, "Задача не была изменена");
        assertEquals(setTaskDto.name(), savedTask.getName());
    }

    @Test
    @DisplayName("Проверка GET /api/v1/task")
    void testGetTask() {
        taskRepository.save(mapperTask.taskDtoToTask(dto)).block();
        Task task = taskRepository.findByName(dto.name()).blockFirst();
        assertNotNull(task, "Ошибка сохранения задачи");
        Flux<TaskDto> taskDtoFlux = webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/v1/task")
                                .queryParam("groupId", dto.groupId())
                                .queryParam("timeZone", "Europe/Moscow")
                                .build()
                )
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
        webTestClient
                .mutateWith(mockJwt().jwt(jwt))
                .delete()
                .uri("/api/v1/task/" + task.getId())
                .exchange()
                .expectStatus().isOk();
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> taskRepository.findById(task.getId()).block() == null);


    }
    // Since the group is created in each test, the correctness of the filling will be checked in this test.
    @Test
    @DisplayName("Проверка POST /api/v1/group")
    void testCreateGroup(){
        Group group1 = groupRepository.findByName(group.getName()).blockFirst();
        assertNotNull(group1);
        assertEquals(group1.getName(), group.getName());
        assertEquals(group1.getDescription(), group.getDescription());
        assertEquals(group1.getId(), group.getId());
    }

    @Test
    @DisplayName("Проверка PATCH /api/v1/group")
    void testSetGroup(){
        SetGroupDto setGroupDto = new SetGroupDto(group.getId(), "Компания изменена", "Описание появилось");

        webTestClient.mutateWith(mockJwt().jwt(jwt))
                .patch()
                .uri("/api/v1/group")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(setGroupDto)
                .exchange()
                .expectStatus().isOk();
        Group group1 = groupRepository.findById(group.getId()).block();
        assertNotNull(group1);
        assertNotEquals(group1.getName(), group.getName());
        assertNotEquals(group1.getDescription(), group.getDescription());
        assertEquals(group1.getId(), group.getId());
    }

    @Test
    @DisplayName("Проверка GET /api/v1/group")
    void testGetGroups(){

        Mono<Map<String, List<GroupDto>>> listGroupDtoFlux = webTestClient.mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri("/api/v1/group")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(new ParameterizedTypeReference<Map<String, List<GroupDto>>>(){})
                .getResponseBody()
                .single();
        StepVerifier.create(listGroupDtoFlux)
                .assertNext(map -> assertNotNull(map.get("groups")))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Проверка PATCH /api/v1/group/role")
    void testSetRoleUserInGroup(){
        // Initially, we save the user, because in the future we will change his role.
        UUID userId = UUID.randomUUID();
        groupUsersRepository.save(GroupUsers.builder().userId(userId).groupId(group.getId()).roleId(userRoleService.findRoleIdByName("MEMBER").block()).build()).block();

        SetUserRole setUserRole = new SetUserRole(group.getId(), userId, "ADMIN");
        webTestClient.mutateWith(mockJwt().jwt(jwt))
                .patch()
                .uri("/api/v1/group/role")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(setUserRole)
                .exchange()
                .expectStatus()
                .isOk();
        Flux<UserDto> lsUserDto = groupUsersRepository.findUserIdByGroupId(group.getId());

        StepVerifier.create(lsUserDto.filter(userDto -> userDto.userId().equals(userId)))
                .assertNext(userDto -> assertEquals("ADMIN", userDto.role()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Проверка DELETE /api/v1/group/{id}")
    void testDeleteGroup(){

        webTestClient.mutateWith(mockJwt().jwt(jwt))
                .delete()
                .uri("/api/v1/group/" + group.getId())
                .exchange()
                .expectStatus().isOk();
        Group group1 = groupRepository.findByName(group.getName()).blockFirst();
        assertNull(group1);
        /* Since each test creates a task for the group,
        it is therefore worth checking that this task was also deleted when it was deleted. */
        Task ts = taskRepository.findById(task.getId()).block();
        assertNull(ts);
    }


    @Test
    @DisplayName("Проверка DELETE /api/v1/group/{groupId}/{userId}")
    void testForcedDeleteUserInGroup(){
        // Initially, we are saving the user, because in the future we will forcibly delete it.
        UUID userId = UUID.randomUUID();
        groupUsersRepository.save(GroupUsers.builder().userId(userId).groupId(group.getId()).roleId(userRoleService.findRoleIdByName("MEMBER").block()).build()).block();

        webTestClient.mutateWith(mockJwt().jwt(jwt))
                .delete()
                .uri("/api/v1/group/" + group.getId() + "/" + userId)
                .exchange()
                .expectStatus()
                .isOk();

        Flux<UserDto> lsUserDto = groupUsersRepository.findUserIdByGroupId(group.getId());
        StepVerifier.create(lsUserDto.filter(userDto -> userDto.userId().equals(userId)))
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Проверка POST /api/v1/group/user/{groupId} и POST /api/v1/group/user/invite/{userId}")
    void testCreateTokenAndLoginWithHim(){

        Flux<UUID> token = webTestClient.mutateWith(mockJwt().jwt(jwt))
                .post()
                .uri("/api/v1/group/user/" + group.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(UUID.class)
                .getResponseBody();
        UUID tokenEntry = token.blockFirst();

        UUID userId = UUID.randomUUID();
        Jwt jwt1 = Jwt.withTokenValue("dummy-token")
                .header("alg", "none")
                .claim("sub", userId)
                .build();
        webTestClient.mutateWith(mockJwt().jwt(jwt1))
                .post()
                .uri("/api/v1/group/user/invite/" + tokenEntry)
                .exchange()
                .expectStatus()
                .isOk();

        Flux<UserDto> lsUserDto = groupUsersRepository.findUserIdByGroupId(group.getId());
        StepVerifier.create(lsUserDto.filter(userDto -> userDto.userId().equals(userId)))
                .assertNext(userDto -> assertEquals("MEMBER", userDto.role()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Проверка GET /api/v1/group/user")
    void testGetGroupsThatUserBelongs(){
        // Creating another group for improved verification
        Flux<Long> flux = webTestClient.mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri("/api/v1/group/user")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Long.class)
                .getResponseBody();
        StepVerifier.create(flux.collectList())
                .assertNext(ls -> assertFalse(ls.isEmpty()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Проверка PATCH /api/v1/user/task/{taskId}")
    void testCompleteTask(){
        webTestClient.mutateWith(mockJwt().jwt(jwt))
                .patch()
                .uri("/api/v1/user/task/" + task.getId())
                .exchange()
                .expectStatus()
                .isOk();
        Mono<Task> taskDtoMono = taskRepository.findById(task.getId());
        StepVerifier.create(taskDtoMono)
                .assertNext(task1 -> assertTrue(task1.getComplete()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Проверка GET /api/v1/user/task")
    void testGetTaskForUser(){
        Flux<TaskDto> taskDtoFlux = webTestClient.mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path("/api/v1/user/task")
                                .queryParam("groupId", group.getId())
                                .queryParam("timeZone", "Europe/Moscow")
                                .build()
                )
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(TaskDto.class)
                .getResponseBody();
        StepVerifier.create(taskDtoFlux)
                .expectNextCount(1)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Проверка POST /api/v1/group/user/{user_id}/common")
    void testCommonGroup(){
        UUID userId = UUID.randomUUID();
        groupUsersRepository.save(GroupUsers.builder().userId(userId).groupId(group.getId()).roleId(userRoleService.findRoleIdByName("MEMBER").block()).build()).block();

        Flux<Boolean> flx = webTestClient.mutateWith(mockJwt().jwt(jwt))
                .post()
                .uri("/api/v1/group/user/" + userId + "/common")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Boolean.class)
                .getResponseBody();


        StepVerifier.create(flx)
                .assertNext(Assertions::assertTrue)
                .expectComplete()
                .verify();
    }
}
