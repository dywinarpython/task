package org.project.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.dto.CreateNotificationDto;
import org.project.dto.ListNotificationDto;
import org.project.dto.kafka.NotificationPayload;
import org.project.mapper.NotificationMapper;
import org.project.repository.NotificationRepository;
import org.project.service.NotificationService;
import org.project.websocket.NotificationsWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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

    private final NotificationsWebSocketHandler notificationsWebSocketHandler;

    @Override
    public Mono<String> countNotificationByUserId(Jwt jwt) {
        return notificationRepository.countByUserIdAndReadFalse(UUID.fromString(jwt.getSubject())).flatMap(count -> {
            if(count >= 100){
                return Mono.just("100+");
            }
            return Mono.just(count.toString());
        });
    }

    @Override
    public Mono<ListNotificationDto> findNotificationsByUserId(Jwt jwt, String timeZone, Integer page) {
        return notificationRepository.findByUserId(UUID.fromString(jwt.getSubject()), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(notificationDto -> notificationMapper.setCreateAtWithTimeZone(notificationDto, timeZone)).collectList()
                .map(ListNotificationDto::new);
    }

    @Override
    public Mono<Void> saveNotification(CreateNotificationDto createNotificationDto) {
        return notificationRepository
                .save(notificationMapper.createNotificationDtoToNotification(createNotificationDto))
                .then(notificationsWebSocketHandler.sendMessageToUser(createNotificationDto.userId(), createNotificationDto.message()));
    }

    @Override
    public Mono<Void> saveAllNotifications(NotificationPayload notificationPayload) {
        return notificationRepository.saveAll(notificationMapper.createNotificationPayloadToNotification(notificationPayload))
                .thenMany(Flux.fromIterable(notificationPayload.userIds()))
                .flatMap(uuid -> notificationsWebSocketHandler.sendMessageToUser(uuid, notificationPayload.message()))
                .then();
    }
    @Override
    public Mono<Void> readNotifications(List<String> notificationsId, Jwt jwt) {
        return notificationRepository.updateField(notificationsId, jwt);
    }
}
