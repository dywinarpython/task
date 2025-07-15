package org.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.project.dto.CreateNotificationDto;
import org.project.dto.ListNotificationDto;
import org.project.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Tag(name = "Управление уведомлениями пользователей")
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Получение уведомлений для определенного пользователя",
            parameters = {
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
                    description = "Список уведомлений",
                    content = @Content(schema = @Schema(implementation = ListNotificationDto.class))
            )
    )
    @GetMapping
    public Mono<ResponseEntity<ListNotificationDto>> getNotificationByUserID(@RequestParam String timeZone, @RequestParam Integer page, @AuthenticationPrincipal Jwt jwt){
        return notificationService.findNotificationsByUserId(jwt, timeZone, page).map(ResponseEntity::ok);
    }


    @Operation(
            summary = "Прочитать уведомления"

    )
    @PostMapping("/read")
    public Mono<ResponseEntity<Void>> markNotificationsRead(@RequestBody List<String> notificationIds, @AuthenticationPrincipal Jwt jwt) {
        return notificationService.readNotifications(notificationIds, jwt)
                .thenReturn(ResponseEntity.ok().build());
    }


}
