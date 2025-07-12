package org.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.util.UUID;

@SpringBootTest
@AutoConfigureWebTestClient
public class UserServiceTest{

    @Autowired
    private WebTestClient webTestClient;

    Jwt jwt = Jwt.withTokenValue("dummy-token")
            .claim("sub", UUID.randomUUID())
            .header("alg", "none")
            .build();

    @Test
    @DisplayName("Проверка GET /api/v1/user/{userId}")
    void testGetUser(){
        webTestClient.mutateWith(mockJwt().jwt(jwt))
                .get()
                .uri("/api/v1/user/" + UUID.randomUUID())
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }
}
