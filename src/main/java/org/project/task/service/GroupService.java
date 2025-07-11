package org.project.task.service;

import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.dto.response.group.ListGroupDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;


public interface GroupService {
    Mono<Void> saveGroup(Mono<CreateGroupDto> createGroupDtoMono, Jwt jwt);
    Mono<Void> setGroup(Mono<SetGroupDto> setGroupDtoMono, Jwt jwt);
    Mono<Void> delGroup(Long id, Jwt jwt);
    Mono<ListGroupDto> getGroups(Jwt jwt);
}
