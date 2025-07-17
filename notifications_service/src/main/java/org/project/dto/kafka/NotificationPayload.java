package org.project.dto.kafka;

import java.util.List;
import java.util.UUID;

public record NotificationPayload(String message, List<UUID> userIds) {
}
