package org.project.service.client;

import org.project.dto.UserDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SearchUserClient {
    Mono<UserDto> searchUser(UUID userId);
}
