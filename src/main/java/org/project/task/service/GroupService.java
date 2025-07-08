package org.project.task.service;

import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.dto.response.group.GroupDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GroupService {
    Mono<Void> saveGroup(Mono<CreateGroupDto> createGroupDtoMono, Jwt jwt);
    Mono<Void> setTask(Mono<SetGroupDto> setGroupDtoMono, Jwt jwt);
    Mono<Void> delGroup(Long id, Jwt jwt);
    Mono<List<GroupDto>> getGroups(Jwt jwt);
    Mono<Void> verifyUserAccess(Long groupId, Jwt jwt);
}
