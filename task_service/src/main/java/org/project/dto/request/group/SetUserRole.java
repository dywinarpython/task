package org.project.dto.request.group;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record SetUserRole(
        @NotNull(message = "Поле groupId не может быть null") Long groupId,
        @NotNull(message = "Поле userId не может быть null") UUID userId,
        @NotNull(message = "Поле nameRole не может быть null")
        @Pattern(regexp = "ADMIN|MEMBER", message = "Допустимы только значения ADMIN или MEMBER")
        String nameRole) {
}
