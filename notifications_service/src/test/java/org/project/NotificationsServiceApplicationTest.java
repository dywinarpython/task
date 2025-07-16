package org.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.dto.CreateNotificationDto;
import org.project.dto.ListNotificationDto;
import org.project.entity.Notification;
import org.project.mapper.NotificationMapper;
import org.project.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, MongoDbTestConfig.class})
public class NotificationsServiceApplicationTest {
    @MockitoBean
    private ConsumerFactory<?, ?> uuidConsumerFactory;
    @MockitoBean
    private ConcurrentKafkaListenerContainerFactory<?, ?> uuidKafkaListenerFactory;

    @MockitoBean
    private ConsumerFactory<?, ?> longConsumerFactory;
    @MockitoBean
    private ConcurrentKafkaListenerContainerFactory<?, ?> longKafkaListenerFactory;

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0")
            .withExposedPorts(27017);
    static {
        mongoDBContainer.start();
    }


    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getConnectionString() + "/testdb");
    }


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationMapper notificationMapper;

    Jwt jwt = Jwt.withTokenValue("dummy-token")
            .claim("sub", UUID.randomUUID())
            .header("alg", "none")
            .build();
    CreateNotificationDto dto;
    Notification notification;

    @BeforeEach
    void createFiled(){
        dto = new CreateNotificationDto("Новое уведомление", UUID.fromString(jwt.getSubject()));
        notification = notificationRepository.save(notificationMapper.createNotificationDtoToNotification(dto)).block();
    }

    @DisplayName("Проверка GET /api/v1/notification")
    @Test
    void testGetNotification(){
        Mono<ListNotificationDto> ls =  webTestClient.mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri(uri->
                        uri.path("/api/v1/notification")
                                .queryParam("timeZone", "Europe/Moscow")
                                .queryParam("page", 0)
                                .build()
                ).exchange()
                .expectStatus()
                .isOk()
                .returnResult(ListNotificationDto.class)
                .getResponseBody()
                .single();
        StepVerifier.create(ls)
                .assertNext(list -> assertFalse(list.notifications().isEmpty()))
                .expectComplete()
                .verify();
    }


    @DisplayName("Проверка GET /api/v1/notification/count")
    @Test
    void testGetNotificationCount(){
        Mono<Map<String, String>> mp =  webTestClient.mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri("/api/v1/notification/count").exchange()
                .expectStatus()
                .isOk()
                .returnResult(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .getResponseBody()
                .single();
        StepVerifier.create(mp)
                .assertNext(count -> assertNotEquals("0", count.get("count")))
                .expectComplete()
                .verify();
    }


    @DisplayName("Проверка POST /api/v1/notification/read")
    @Test
    void testPostNotificationRead(){
        webTestClient.mutateWith(mockJwt().jwt(jwt))
                .post()
                .uri("/api/v1/notification/read")
                .bodyValue(List.of(notification.getId()))
                .exchange()
                .expectStatus()
                .isOk();

        Notification notification1 = notificationRepository.findById(notification.getId()).block();
        assert notification1 != null;
        assertTrue(notification1.isRead());
    }
}
