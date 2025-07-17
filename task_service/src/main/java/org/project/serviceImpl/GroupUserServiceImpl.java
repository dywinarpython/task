package org.project.serviceImpl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.group.SetUserRole;
import org.project.dto.response.group.ListUserDto;
import org.project.entity.GroupUsers;
import org.project.repository.GroupUsersRepository;
import org.project.service.GroupUserService;
import org.project.service.KafkaService;
import org.project.service.UserRoleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupUserServiceImpl implements GroupUserService {

    @Value("${pageable.size}")
    private Long size;


    private final GroupUsersRepository groupUsersRepository;

    private final UserRoleService userRoleService;

    private final CacheManager cacheManager;

    private final KafkaService kafkaService;

    private final TransactionalOperator transactionalOperator;


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
                .flatMap(groupId -> checkCountUserInGroup(groupId).thenReturn(groupId))
                .flatMap(groupId -> groupUsersRepository.existsByGroupIdAndUserId(groupId, UUID.fromString(jwt.getSubject())).flatMap(ex -> {
                    if (ex) {
                        return Mono.error(new ValidationException("The user has already been added to the group"));
                    }
                    return saveUserInGroup(jwt, groupId, "MEMBER");
                }).then(kafkaService.sendMessageToNotifications(UUID.fromString(jwt.getSubject()), "Вы добавлены в новую группу."))
                                .as(transactionalOperator::transactional)
                );
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
    public Mono<Void> saveUserInGroup(Jwt jwt, Long groupId, String role) {
        return checkCountUserInGroup(groupId)
                .then(userRoleService.findRoleIdByName(role)
                        .flatMap(roleID ->
                                groupUsersRepository.save(
                                        GroupUsers.builder()
                                                .userId(UUID.fromString(jwt.getSubject()))
                                                .groupId(groupId)
                                                .roleId(roleID)
                                                .build()
                                )
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


    @Cacheable(value = "GROUPS_ID", key = "#userId")
    @Override
    public Mono<List<Long>> getGroupsId(UUID userId) {
        return groupUsersRepository.findGroupsIdByUserID(userId).collectList();
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
    public Mono<ListUserDto> getAllUserForGroup(Long groupId, Jwt jwt, Long page) {
        return verifyAdminAccess(groupId, jwt).then( Mono.defer(() -> {
            Cache cache = cacheManager.getCache("USER_IN_GROUP");
            if(cache == null){
                return Mono.error(new RuntimeException("Кеш не доступен"));
            }
            ListUserDto ls = cache.get(groupId + " " + page, ListUserDto.class);
            if(ls != null){
                return Mono.just(ls);
            }
            return groupUsersRepository
                    .findUserByGroupId(groupId, size, size*page)
                    .collectList()
                    .map(ListUserDto::new)
                    .doOnNext(lsUser -> cache.put(groupId + " " + page, lsUser));
        }));
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
    private Mono<Void> checkCountUserInGroup(Long groupId){
        return groupUsersRepository.checkGroupSize(groupId)
                .count()
                .doOnNext(c -> log.info("Количество учатсников: {}", c))
                .flatMap(count -> {
                    if(count > 20){
                        return Mono.error(new ValidationException("Количество участников группы не больше 20!"));
                    }
                    return Mono.just(0);
                }).then();
    }


}

