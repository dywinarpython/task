package org.project.dto.request.task;

import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

public record SetTaskDto(
        @NotNull(message = "ID задачи не может быть null") Long id,
        @Size(max = 50, min = 3, message = "Длины поле name от 3 до 50") String name,
        @Size(max = 10_000, min = 3,  message = "Длины поле description от 3 до 10_000") String description,
        @Future(message = "Дата завершения должна быть в будущем")
        OffsetDateTime deadLine,
        @Size(min = 3, max = 20,  message = "Длины status name от 3 до 20") String status) {
}
