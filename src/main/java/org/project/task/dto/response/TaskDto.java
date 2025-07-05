package org.project.task.dto.response;



import java.time.LocalDateTime;

public record TaskDto(
        Long id,
        String name,
        String description,
        LocalDateTime deadLine,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        String status) {
}
