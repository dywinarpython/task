package org.project.dto.request.task;

import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTaskWithUserDto(
        UUID userID,
        @NotNull(message = "Поле id_group не может быть null") Long groupId,
        @NotNull(message = "Поле name не может быть null") @Size(max = 50, min = 3, message = "Длины поле name от 3 до 50") String name,
        @Size(max = 10_000, min = 3,  message = "Длины поле description от 3 до 10_000") @NotNull(message = "Поле description не может быть null") String description,
        @NotNull(message = "Поле deadLine не может быть null")
        @Future(message = "Срок выполнения задачи должен быть в будущем")
        OffsetDateTime deadLine,
        @NotNull(message = "Поле status не может быть null") @Size(min = 3, max = 20,  message = "Длины status name от 3 до 20") String status) {
}
