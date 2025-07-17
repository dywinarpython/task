package org.project.dto.response.task;


import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskWithUserDto (Long id,
                               String name,
                               String description,
                               OffsetDateTime deadLine,
                               LocalDateTime createTime,
                               LocalDateTime updateTime,
                               String status,
                               Boolean complete,
                               UUID assignee,
                               UUID assignBy,
                               Boolean isActiveUser){
}