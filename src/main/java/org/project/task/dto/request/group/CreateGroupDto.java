package org.project.task.dto.request.group;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupDto(@NotNull(message = "Поле name не может быть null") @Size(max = 30, min = 3, message = "Длины поле name от 3 до 30") String name,
                             @Size(max = 1000, min = 3,  message = "Длины поле description от 3 до 1000") @NotNull(message = "Поле description не может быть null") String description) {
}
