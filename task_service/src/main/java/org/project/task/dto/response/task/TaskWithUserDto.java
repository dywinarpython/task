package org.project.task.dto.response.task;


import java.time.LocalDateTime;
import java.util.UUID;

public record TaskWithUserDto (Long id,
                               String name,
                               String description,
                               LocalDateTime deadLine,
                               LocalDateTime createTime,
                               LocalDateTime updateTime,
                               String status,
                               Boolean complete,
                               UUID assignee,
                               Boolean isActiveUser){
}
