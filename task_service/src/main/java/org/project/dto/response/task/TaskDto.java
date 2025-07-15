package org.project.dto.response.task;



import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDto(
        Long id,
        UUID assignBy,
        String name,
        String description,
        LocalDateTime deadLine,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        String status,
        Boolean complete) {
}
