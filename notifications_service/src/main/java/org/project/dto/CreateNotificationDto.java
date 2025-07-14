package org.project.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateNotificationDto(
        @NotNull String message,
        @NotNull UUID userId) {

}
