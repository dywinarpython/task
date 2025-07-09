package org.project.task.dto.response.task;



import java.time.LocalDateTime;

public record TaskDto(
        Long id,
        String name,
        String description,
        LocalDateTime deadLine,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        String status,
        Boolean complete) {
}
