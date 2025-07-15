package org.project.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.project.dto.CreateNotificationDto;
import org.project.dto.kafka.NotificationPayload;
import org.project.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationsHandler {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notifications-user-task", containerFactory = "uuidKafkaListenerFactory")
    public void createNotificationsForUser(ConsumerRecord<UUID, String> consumerRecord){
        notificationService.saveNotification(new CreateNotificationDto(consumerRecord.value(), consumerRecord.key())).subscribe();
    }

    @KafkaListener(topics = "notifications-user-group-task", containerFactory = "longKafkaListenerFactory")
    public void createNotificationsForAllUserInGroup(ConsumerRecord<Long, NotificationPayload> consumerRecord){
        notificationService.saveAllNotifications(consumerRecord.value()).subscribe();
    }
}
