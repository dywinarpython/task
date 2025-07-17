package org.project.service;

import org.project.dto.request.group.SetUserRole;
import org.project.dto.response.group.ListUserDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface GroupUserService {

    Mono<Void> saveUserInGroupWithToken(Jwt jwt, UUID token);
    Mono<UUID> generateTokenToEnterGroup(Jwt jwt, Long groupId);
    Mono<Void> saveUserInGroup(Jwt jwt, Long groupID, String role);
    Mono<Void> leaveGroup(Jwt jwt, Long groupId);
    Mono<Void> deleteUserInGroup(Jwt jwt, UUID userId, Long groupId);
    Mono<Boolean> checkUserInGroup(Long groupId, UUID userId);
    Mono<List<Long>> getGroupsId(UUID userId);
    Mono<Void> verifyAdminAccess(Long groupId, Jwt jwt);
    Mono<Void> verifyOwnerAccess(Long groupId, Jwt jwt);
    Mono<Void> assigningRights(Jwt jwt, Mono<SetUserRole> setUserRoleMono);
    Mono<ListUserDto> getAllUserForGroup(Long groupId, Jwt jwt, Long page);
}
