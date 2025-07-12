package org.project.serviceImpl.client;

import org.project.service.client.UserClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.UUID;
@Service
public class UserClientImpl implements UserClient {

    private final WebClient webClient;

    public UserClientImpl(@Qualifier("defaultWebClientBuilder") WebClient.Builder webClient) {
        this.webClient = webClient.baseUrl("http://localhost:9090").build();
    }

    @Override
    public Mono<Void> checkCommonGroups(Jwt jwt, UUID userId) {
        return webClient
                .post()
                .uri("api/v1/group/user/" + userId + "/common")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .exchangeToMono(clientResponse -> {
                    if(clientResponse.statusCode().is2xxSuccessful()){
                        return clientResponse.bodyToMono(Boolean.class).flatMap(bl -> {
                            if(bl){
                                return Mono.empty();
                            }
                            return Mono.error(new NoSuchElementException("Пользователь не найден, возможно его больше нет в группе, или он удалил аккаунт"));
                        });
                    } else {
                        return clientResponse.bodyToMono(String.class).defaultIfEmpty("Ошибка при поиске пользователя").flatMap(errorBody -> Mono.error(new Exception(errorBody)));
                    }
                });
    }
}
