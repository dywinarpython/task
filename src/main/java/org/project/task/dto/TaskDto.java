package org.project.task.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskDto(
        Long id,
        String name,
        String description,
        LocalDateTime deadLine,
        LocalDateTime createTime,
        String status) {
}
