package org.project.task.serviceImpl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.task.dto.request.group.SetUserRole;
import org.project.task.entity.GroupUsers;
import org.project.task.repository.GroupUsersRepository;
import org.project.task.service.GroupUserService;
import org.project.task.service.UserRoleService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupUserServiceImpl implements GroupUserService {

    private final GroupUsersRepository groupUsersRepository;

    private final UserRoleService userRoleService;

    private final CacheManager cacheManager;


    @CacheEvict(value = "GROUPS_ID", key = "#jwt.getSubject()")
    @Override
    public Mono<Void> saveUserInGroupWithToken(Jwt jwt, UUID token) {
        return Mono.fromCallable(() -> {
            Cache cache = cacheManager.getCache("GROUPS_ID_BY_UUID");
            if(cache != null){
                return cache.get(token, Long.class);
            }
            return null;
        })
        .switchIfEmpty(Mono.error(new ValidationException("The token has expired or invalid")))
        .flatMap(groupId -> groupUsersRepository.existsByGroupIdAndUserId(groupId, UUID.fromString(jwt.getSubject())).flatMap(ex -> {
            if (ex) {
                return Mono.error(new ValidationException("The user has already been added to the group"));
            }
            return saveUserInGroup(jwt, groupId, "MEMBER");
        })).then();
    }

    @Override
    public Mono<UUID> generateTokenToEnterGroup(Jwt jwt, Long groupId) {
        return verifyAdminAccess(groupId, jwt)
                .then(Mono.fromSupplier(UUID::randomUUID))
                .doOnNext(uuid -> {
                    Cache cache = cacheManager.getCache("GROUPS_ID_BY_UUID");
                    if (cache != null) {
                        cache.put(uuid, groupId);
                    } else {
                        log.error("Кеш не доступен");
                    }
                });
    }


    @CacheEvict(value = "GROUPS_ID", key = "#jwt.getSubject()")
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

    @CacheEvict(value = "GROUPS_ID", key = "#jwt.getSubject()")
    @Override
    public Mono<Void> leaveGroup(Jwt jwt, Long groupId) {
        return userRoleService.findRoleIdByName("OWNER").flatMap(roleId ->
                groupUsersRepository.deleteByUserIDAndByGroupId(groupId, UUID.fromString(jwt.getSubject()), roleId)
                .flatMap(count -> {
                    if(count == 0) {
                        return Mono.error(new ValidationException("You cannot leave the group, you are either not a member of this group, or you own it."));
                    }
                    return Mono.empty();})).then();
    }

    @CacheEvict(value = "GROUPS_ID", key = "#userId.toString()")
    @Override
    public Mono<Void> deleteUserInGroup(Jwt jwt, UUID userId, Long groupId) {
        return verifyAdminAccess(groupId, jwt)
                .then(
                        userRoleService.findRoleIdByName("OWNER").flatMap( roleId ->
                        groupUsersRepository.deleteByUserIDAndByGroupId(groupId, userId, roleId).flatMap(count -> {
                            if(count == 0) {
                                return Mono.error(new ValidationException("It was not possible to delete the user from the group, " +
                                        "most likely, the user is not a member of the group, or he is the owner himself"));
                            }
                            return Mono.empty();
                        }
                ))).then();
    }

    @Override
    public Mono<Boolean> checkUserInGroup(Long groupId, UUID userId) {
            if(userId == null){
                return Mono.error(new ValidationException("User not found!"));
            }
            return groupUsersRepository.existsByGroupIdAndUserId(groupId, userId)
                    .filter(exists -> exists)
                    .switchIfEmpty(Mono.error(new NoSuchElementException("The user is not in the group with the id:" + groupId)));
        }


    @Cacheable(value = "GROUPS_ID", key = "#jwt.getSubject()")
    @Override
    public Mono<List<Long>> getGroupsId(Jwt jwt) {
        return groupUsersRepository.findGroupsIdByUserID(UUID.fromString(jwt.getSubject())).collectList();
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

    @Override
    public Mono<Void> assigningRights(Jwt jwt, Mono<SetUserRole> setUserRoleMono) {
        return setUserRoleMono.flatMap( setUserRole -> {
            if(setUserRole.userId().equals(UUID.fromString(jwt.getSubject()))){
                return Mono.error(new ValidationException("The user cannot assign a role to himself"));
            }
        return verifyOwnerAccess(setUserRole.groupId(), jwt)
                .then(userRoleService.findRoleIdByName(setUserRole.nameRole()))
                .flatMap(roleId ->
                        groupUsersRepository.assigningRights(setUserRole.groupId(), setUserRole.userId(), roleId))
                .flatMap(count -> {
                    if (count == 0){
                        return this.checkUserInGroup(setUserRole.groupId(), setUserRole.userId());
                    }
                    return Mono.empty();
                });}).then();
    }


    @Override
    public Mono<List<UUID>> getAllUserForGroup(Long groupId, Jwt jwt) {
        return verifyAdminAccess(groupId, jwt).then(groupUsersRepository.findUserIDByGroupId(groupId).collectList());
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

