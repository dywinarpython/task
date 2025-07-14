package org.project.repository;


import org.project.dto.NotificationDto;
import org.project.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, String>, UpdateFiledRepository{
    Flux<NotificationDto> findByUserId(UUID userId, Pageable pageable);


}
