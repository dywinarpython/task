package org.project.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.dto.request.group.SetUserRole;
import org.project.task.dto.response.group.ListGroupDto;
import org.project.task.dto.response.group.ListUserDto;
import org.project.task.dto.response.group.UserDto;
import org.project.task.service.GroupService;
import org.project.task.service.GroupUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
@Tag(name = "Управление группами")
public class GroupController {
    private final GroupService groupService;

    private final GroupUserService groupUserService;

    @Operation(
            summary = "Создания группы",
            responses = @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createGroup(@RequestBody @Valid Mono<CreateGroupDto> createGroupDtoMono,
                                                                @AuthenticationPrincipal Jwt jwt){
        return groupService.saveGroup(createGroupDtoMono, jwt)
                .thenReturn(ResponseEntity.status(201).body(Map.of("message", "Группа сохранена")));
    }

    @Operation(
            summary = "Изменение группы",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PatchMapping
    public Mono<ResponseEntity<Map<String, String>>> setGroup(@RequestBody @Valid Mono<SetGroupDto> setGroupDtoMono, @AuthenticationPrincipal Jwt jwt){
        return groupService.setGroup(setGroupDtoMono, jwt).thenReturn(ResponseEntity.ok(Map.of("message", "Группа измененна")));
    }

    @Operation(
            summary = "Изменение прав пользователей для определенной группы",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @PatchMapping("/role")
    public Mono<ResponseEntity<Map<String, String>>> setUserRoleGroup(@RequestBody @Valid Mono<SetUserRole> setUserRoleMono, @AuthenticationPrincipal Jwt jwt){
        return groupUserService.assigningRights(jwt, setUserRoleMono).thenReturn(ResponseEntity.ok(Map.of("message", "Прав пользователя изменены")));
    }


    @Operation(
            summary = "Удаление группы",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteGroup(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return groupService.delGroup(id, jwt)
                .thenReturn(ResponseEntity.status(200).body(Map.of("message", "Группа удалена")));
    }

    @Operation(
            summary = "Принудителное удаление пользователя из группы, назначаннные задачи к нему остануться",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{groupId}/{userId}")
    public Mono<ResponseEntity<Map<String, String>>> deleteUserInGroup(@PathVariable Long groupId, @PathVariable UUID userId, @AuthenticationPrincipal Jwt jwt){
        return groupUserService.deleteUserInGroup(jwt, userId, groupId)
                .thenReturn(ResponseEntity.status(200).body(Map.of("message", "Пользователь удален с группы")));
    }

    @Operation(
            summary = "Получение групп созданных пользователем",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ListGroupDto.class)))
    )
    @GetMapping
    public Mono<ResponseEntity<ListGroupDto>> getGroups(@AuthenticationPrincipal Jwt jwt){
        return groupService.getGroups(jwt).map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Получение участников группы",
            responses = @ApiResponse(
            responseCode = "200",
            description = "Список участников группы",
            content = @Content(
                    schema = @Schema(implementation = ListUserDto.class)
            ))
    )
    @GetMapping("/{groupId}")
    public Mono<ResponseEntity<List<UserDto>>> getUserFroGroup(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt){
        return groupUserService.getAllUserForGroup(groupId, jwt).map(ResponseEntity::ok);
    }
}
