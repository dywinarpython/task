package org.project.service;

import org.project.dto.CreateNotificationDto;
import org.project.dto.ListNotificationDto;
import org.project.dto.kafka.NotificationPayload;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;

public interface NotificationService {


    Mono<String> countNotificationByUserId(Jwt jwt);

    Mono<ListNotificationDto> findNotificationsByUserId(Jwt jwt, String timeZone, Integer page);

    Mono<Void> saveNotification(CreateNotificationDto createNotificationDto);

    Mono<Void> saveAllNotifications(NotificationPayload notificationPayload);

    Mono<Void> readNotifications(List<String> notificationsId, Jwt jwt);
}
