package org.project.task.serviceImpl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.project.task.entity.GroupUsers;
import org.project.task.repository.GroupUsersRepository;
import org.project.task.service.GroupUserService;
import org.project.task.service.UserRoleService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupUserServiceImpl implements GroupUserService {

    private final GroupUsersRepository groupUsersRepository;

    private final UserRoleService userRoleService;



    @Override
    public Mono<Void> saveUserInGroup(Jwt jwt, Long groupID, String role) {
        return userRoleService.findRoleIdByName(role).flatMap(roleID ->
                groupUsersRepository.save(
                        GroupUsers.builder()
                                .userId(UUID.fromString(jwt.getSubject()))
                                .groupId(groupID)
                                .roleId(roleID)
                                .build()
                )
        ).then();
    }

    @Override
    public Mono<Boolean> validateUserInGroup(Long groupId, UUID userId) {
            if(userId == null){
                return Mono.error(new ValidationException("User not found!"));
            }
            return groupUsersRepository.existsByGroupIdAndUserId(groupId, userId)
                    .filter(exists -> exists)
                    .switchIfEmpty(Mono.error(new NoSuchElementException("The user is not in the group with the id:" + groupId)));
        }
    @Override
    public Flux<Long> getGroupsId(Jwt jwt) {
        return groupUsersRepository.findGroupsIdByUserID(UUID.fromString(jwt.getSubject()));
    }

    @Override
    public Mono<Void> verifyAdminAccess(Long groupId, Jwt jwt) {
        return Mono.zip(userRoleService.findRoleIdByName("ADMIN"), userRoleService.findRoleIdByName("OWNER")).flatMap(tuple ->
                verifyRoleAccess(groupId, jwt, List.of(tuple.getT1(), tuple.getT2()))
                );
    }

    @Override
    public Mono<Void> verifyOwnerAccess(Long groupId, Jwt jwt) {
        return userRoleService.findRoleIdByName("OWNER").flatMap(roleId ->
                verifyRoleAccess(groupId, jwt, List.of(roleId))
        );
    }

    private Mono<Void> verifyRoleAccess(Long groupId, Jwt jwt, List<Long> roleId){
        return groupUsersRepository.existsByRoles(UUID.fromString(jwt.getSubject()), groupId, roleId.toArray(Long[]::new))
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return groupUsersRepository.existsByGroupId(groupId)
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return Mono.error(new NoSuchElementException("A group with an id:" + groupId + " not found"));
                                    }
                                    return Mono.error(new AccessDeniedException("Access is denied"));
                                });
                    }
                    return Mono.empty();
                });
    }


}

