package org.project.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        String id,
        String message,
                              Boolean read,
                              LocalDateTime localDateTime) {
}
