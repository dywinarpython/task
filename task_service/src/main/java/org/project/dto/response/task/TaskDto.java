package org.project.dto.response.task;



import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskDto(
        Long id,
        UUID assignBy,
        String name,
        String description,
        OffsetDateTime deadLine,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        String status,
        Boolean complete) {
}
