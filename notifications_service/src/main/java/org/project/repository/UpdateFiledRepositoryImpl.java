package org.project.repository;

import lombok.RequiredArgsConstructor;
import org.project.entity.Notification;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateFiledRepositoryImpl implements UpdateFiledRepository{

    private final ReactiveMongoTemplate mongoTemplate;
    @Override
    public Mono<Void> updateField(List<String> notificationsId, Jwt jwt) {
        Query query = new Query(Criteria.where("id").in(notificationsId).and("userId").is(UUID.fromString(jwt.getSubject())));
        Update update = new Update().set("read", true);
        return mongoTemplate.updateMulti(query, update, Notification.class)
                .then();
    }
}
