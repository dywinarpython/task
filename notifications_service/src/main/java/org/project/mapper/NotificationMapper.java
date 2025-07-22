package org.project.mapper;

import org.project.dto.CreateNotificationDto;
import org.project.dto.NotificationDto;
import org.project.dto.kafka.NotificationPayload;
import org.project.entity.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class NotificationMapper {

    public NotificationDto setCreateAtWithTimeZone(NotificationDto notificationDto, String timeZone){
        LocalDateTime createdAt = notificationDto.createdAt();
        ZonedDateTime zonedDateTimeUtc = createdAt.atZone(ZoneOffset.UTC);
        ZonedDateTime zonedDateTimeTarget = zonedDateTimeUtc.withZoneSameInstant(ZoneId.of(timeZone));
        LocalDateTime converted = zonedDateTimeTarget.toLocalDateTime();
        return new NotificationDto(notificationDto.id(), notificationDto.message(), notificationDto.read(), converted);
    }
    public Notification createNotificationDtoToNotification(CreateNotificationDto createNotificationDto){
        Notification notification = new Notification();
        notification.setMessage(createNotificationDto.message());
        notification.setUserId(createNotificationDto.userId());
        notification.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        return notification;
    }
    public List<Notification> createNotificationPayloadToNotification(NotificationPayload notificationPayload){
        return notificationPayload.userIds().stream().map(userId -> {
                    Notification notification = new Notification();
                    notification.setMessage(notificationPayload.message());
                    notification.setUserId(userId);
                    notification.setCreatedAt(LocalDateTime.now());
                    return notification;}
                ).toList();
    }
}
