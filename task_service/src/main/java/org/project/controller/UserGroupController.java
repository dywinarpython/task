package org.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.project.service.GroupUserService;
import org.project.service.UserCommonGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Управление участников группы")
@RestController
@RequestMapping("/api/v1/group/user")
@RequiredArgsConstructor
public class UserGroupController {

    private final GroupUserService groupUserService;

    private final UserCommonGroupService userCommonGroupService;

    @Operation(
            summary = "Получение id групп, в которые входит пользователь",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Список идентификаторов",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(type = "integer", format = "int64"))
                    )
            )
    )
    @GetMapping
    public Mono<ResponseEntity<List<Long>>> getGroupsId(@AuthenticationPrincipal Jwt jwt) {
        return groupUserService.getGroupsId(UUID.fromString(jwt.getSubject())).map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Выход пользователя из группы, назначенные ему задачи останутся"
    )
    @DeleteMapping("/{groupId}")
    public Mono<ResponseEntity<Void>> leaveGroup(@PathVariable Long groupId,  @AuthenticationPrincipal Jwt jwt){
        return groupUserService.leaveGroup(jwt, groupId).thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Генерация токена доступа к группе"
    )
    @PostMapping("/{groupId}")
    public Mono<ResponseEntity<UUID>> generateToken(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt){
        return groupUserService.generateTokenToEnterGroup(jwt, groupId).map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Вход в группу по токену доступа"
    )
    @PostMapping("/invite/{token}")
    public Mono<ResponseEntity<Map<String, String>>> saveUserInGroupWithToken(@PathVariable UUID token, @AuthenticationPrincipal Jwt jwt){
        return groupUserService.saveUserInGroupWithToken(jwt, token).thenReturn(ResponseEntity.ok(Map.of("message", "Пользователь добавлен в группу")));
    }

    @Operation(
            summary = "Проверка общих групп между пользователями",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Boolean.class)))
    )
    @PostMapping("/{userId}/common")
    public Mono<Boolean> hasCommonGroup(@PathVariable UUID userId, @AuthenticationPrincipal Jwt jwt){
        return userCommonGroupService.hasCommonGroup(jwt, userId);
    }
}
