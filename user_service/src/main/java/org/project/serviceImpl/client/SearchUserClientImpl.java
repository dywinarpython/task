package org.project.serviceImpl.client;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.UserDto;
import org.project.service.client.SearchUserClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class SearchUserClientImpl implements SearchUserClient {



    private final WebClient webClient;

    public SearchUserClientImpl(@Qualifier("oauth2WebClient") WebClient.Builder webClient, AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientServiceReactiveOAuth2AuthorizedClientManager) {
        this.webClient = webClient.baseUrl("http://localhost:8080").build();
    }

    @Cacheable(value = "USER_INFO", key = "#userId")
    @Override
    public Mono<UserDto> searchUser(UUID userId) {
        return webClient.get()
                            .uri("/admin/realms/task/users/" + userId)
                            .exchangeToMono(clientResponse -> {
                                if(clientResponse.statusCode().is2xxSuccessful()){
                                    return clientResponse.bodyToMono(UserDto.class);
                                }
                                else {
                                    return clientResponse.bodyToMono(String.class).defaultIfEmpty("Ошибка при поиске пользователя").flatMap(errorBody -> Mono.error(new Exception(errorBody)));
                                }
                            });
    }
}