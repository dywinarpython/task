package org.project.service;

import reactor.core.publisher.Mono;

import java.util.UUID;



public interface KafkaService {
    Mono<Void> sendMessageToNotifications(UUID userId, String message);
    Mono<Void> sendMessageToAllUserInGroup(Long groupId);
}
