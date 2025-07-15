package org.project.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.service.GroupUserService;
import org.project.service.UserCommonGroupService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCommonGroupServiceImpl implements UserCommonGroupService {

    private final GroupUserService groupUserService;

    @Override
    public Mono<Boolean> hasCommonGroup(Jwt jwt, UUID userId) {
        return
                Mono.zip(groupUserService.getGroupsId(UUID.fromString(jwt.getSubject())), groupUserService.getGroupsId(userId))
                        .map(tuple -> !Collections.disjoint(tuple.getT1(), tuple.getT2()));
    }
}
