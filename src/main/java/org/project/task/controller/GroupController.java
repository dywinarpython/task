package org.project.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.dto.response.group.GroupDto;
import org.project.task.dto.response.group.ListGroupDto;
import org.project.task.service.GroupService;
import org.project.task.service.GroupUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

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
            summary = "Удаление группы",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    )
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteGroup(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return groupService.delGroup(id, jwt)
                .thenReturn(ResponseEntity.status(200).body(Map.of("message", "Группа удалена")));
    }
    @Operation(
            summary = "Получение групп созданных пользователем",
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ListGroupDto.class)))
    )
    @GetMapping
    public Mono<ResponseEntity<Map<String, List<GroupDto>>>> getGroups(@AuthenticationPrincipal Jwt jwt){
        return groupService.getGroups(jwt).map(groups -> ResponseEntity.ok(Map.of("groups", groups)));
    }

    @Operation(
            summary = "Получение id групп, пользователя который в них вошел",
            responses = @ApiResponse(
            responseCode = "200",
            description = "Список идентификаторов",
            content = @Content(array = @ArraySchema(schema = @Schema(type = "integer", format = "int64")))
            )
    )
    @GetMapping("/user")
    public ResponseEntity<Flux<Long>> getGroupsId(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(groupUserService.getGroupsId(jwt));
    }
}
