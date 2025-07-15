package org.project.serviceImpl;

import org.project.dto.kafka.NotificationPayload;
import org.project.repository.GroupRepository;
import org.project.repository.GroupUsersRepository;
import org.project.service.KafkaService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class KafkaServiceImpl implements KafkaService {



    private final KafkaTemplate<Long, NotificationPayload> kafkaTemplateAllGroup;

    private final  KafkaTemplate<UUID, String> kafkaTemplateUser;

    private final GroupUsersRepository groupUsersRepository;

    private final GroupRepository groupRepository;

    public KafkaServiceImpl(@Qualifier("messageToAllUserInGroup") KafkaTemplate<Long, NotificationPayload> kafkaTemplateAllGroup,
                            @Qualifier("messageToUser") KafkaTemplate<UUID, String> kafkaTemplateUser,
                            GroupUsersRepository groupUsersRepository,
                            GroupRepository groupRepository) {
        this.kafkaTemplateAllGroup = kafkaTemplateAllGroup;
        this.kafkaTemplateUser = kafkaTemplateUser;
        this.groupUsersRepository = groupUsersRepository;
        this.groupRepository = groupRepository;
    }


    @Override
    public Mono<Void> sendMessageToNotifications(UUID userId, String message) {
        return Mono.fromFuture(kafkaTemplateUser.send("notifications-user-task", userId, message)).then();
    }
    @Override
    public Mono<Void> sendMessageToAllUserInGroup(Long groupId) {

        return groupUsersRepository.findUserIdByGroupId(groupId).collectList().flatMap(ls ->
                    groupRepository.getNameGroupById(groupId).map(name ->
                        new NotificationPayload("Создана общая задача для группы: " + name, ls))
        ).flatMap(notificationPayload ->
                        Mono.fromFuture(kafkaTemplateAllGroup.send("notifications-user-group-task", groupId, notificationPayload))
                )
                .then();
    }
}
