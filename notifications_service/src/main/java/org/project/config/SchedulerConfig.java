package org.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.entity.Notification;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    private final ReactiveMongoTemplate mongoTemplate;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanReadNotifications() {
        Query query = new Query(Criteria.where("read").is(true));
        mongoTemplate.remove(query, Notification.class)
                .subscribe(result -> SchedulerConfig.log.info("Удалено уведомлений: {}", result.getDeletedCount()));
    }
}
