package org.project.dto.request.group;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SetGroupDto(
        @NotNull(message = "ID группы не может быть null") Long id,
        @Size(max = 30, min = 3, message = "Длины поле name от 3 до 30") String name,
        @Size(max = 1000, min = 3,  message = "Длины поле description от 3 до 1000") String description) {
}
