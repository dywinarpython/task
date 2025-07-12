package org.project.task.dto.request.task;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SetTaskDto(
        @NotNull(message = "ID задачи не может быть null") Long id,
        @Size(max = 50, min = 3, message = "Длины поле name от 3 до 50") String name,
        @Size(max = 10_000, min = 3,  message = "Длины поле description от 3 до 10_000") String description,
        @Max(message = "Максимальный срок выполнения задачи 10 лет", value = 315360000000L )
        @Min(message = "Минимальный срок выполнения задачи 30 минут", value = 1800000L)
        Long deadLine,
        @Size(min = 3, max = 20,  message = "Длины status name от 3 до 20") String status) {
}
