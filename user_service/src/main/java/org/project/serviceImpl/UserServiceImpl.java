package org.project.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.dto.UserDto;
import org.project.service.UserService;
import org.project.service.client.SearchUserClient;
import org.project.service.client.UserClient;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserClient userClient;

    private final SearchUserClient searchUserClient;

    @Override
    public Mono<UserDto> findUserByUserID(UUID userId, Jwt jwt) {
        return userClient.checkCommonGroups(jwt, userId).then(searchUserClient.searchUser(userId));
    }

    @Override
    public Mono<UserDto> findUser(Jwt jwt) {
        return searchUserClient.searchUser(UUID.fromString(jwt.getSubject()));
    }
}
