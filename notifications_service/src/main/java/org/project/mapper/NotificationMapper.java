package org.project.mapper;

import org.project.dto.CreateNotificationDto;
import org.project.dto.NotificationDto;
import org.project.entity.Notification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class NotificationMapper {

    public NotificationDto setCreateAtWithTimeZone(NotificationDto notificationDto, String timeZone){
        return new NotificationDto(notificationDto.id(), notificationDto.message(), notificationDto.read(), notificationDto.localDateTime().atZone(ZoneId.of(timeZone)).toLocalDateTime());
    }
    public Notification createNotificationDtoToNotification(CreateNotificationDto createNotificationDto){
        Notification notification = new Notification();
        notification.setMessage(createNotificationDto.message());
        notification.setUserId(createNotificationDto.userId());
        notification.setLocalDateTime(LocalDateTime.now());
        return notification;
    }
}
