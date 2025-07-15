package org.project.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.dto.CreateNotificationDto;
import org.project.dto.ListNotificationDto;
import org.project.dto.kafka.NotificationPayload;
import org.project.mapper.NotificationMapper;
import org.project.repository.NotificationRepository;
import org.project.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Value("${pageable.size}")
    private int size;

    private final NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper;
    @Override
    public Mono<ListNotificationDto> findNotificationsByUserId(Jwt jwt, String timeZone, Integer page) {
        return notificationRepository.findByUserId(UUID.fromString(jwt.getSubject()), PageRequest.of(page, size))
                .map(notificationDto -> notificationMapper.setCreateAtWithTimeZone(notificationDto, timeZone)).collectList()
                .map(ListNotificationDto::new);
    }

    @Override
    public Mono<Void> saveNotification(CreateNotificationDto createNotificationDto) {
        return notificationRepository.save(notificationMapper.createNotificationDtoToNotification(createNotificationDto)).then();
    }

    @Override
    public Mono<Void> saveAllNotifications(NotificationPayload notificationPayload) {
        return notificationRepository.saveAll(notificationMapper.createNotificationPayloadToNotification(notificationPayload)).then();
    }

    @Override
    public Mono<Void> readNotifications(List<String> notificationsId, Jwt jwt) {
        return notificationRepository.updateField(notificationsId, jwt);
    }
}
