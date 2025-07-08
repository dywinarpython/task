package org.project.task.serviceImpl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.project.task.repository.GroupUsersRepository;
import org.project.task.service.GroupUserService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupUserServiceImpl implements GroupUserService {

    private final GroupUsersRepository groupUsersRepository;
    @Override
    public Mono<Boolean> validateUserInGroup(Long groupId, UUID userId) {
            if(userId == null){
                return Mono.error(new ValidationException("Пользователь не передан!"));
            }
            return groupUsersRepository.existsByGroupIdAndUserId(groupId, userId)
                    .filter(exists -> exists)
                    .switchIfEmpty(Mono.error(new NoSuchElementException("Пользователя нет в группе с id: " + groupId)));
        }
    @Override
    public Flux<Long> getGroupsId(Jwt jwt) {
        return groupUsersRepository.findGroupsIdByUserID(UUID.fromString(jwt.getSubject()));
    }
}

