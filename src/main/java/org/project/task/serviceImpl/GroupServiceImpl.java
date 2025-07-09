package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.dto.response.group.GroupDto;
import org.project.task.entity.Group;
import org.project.task.mapper.group.MapperGroup;
import org.project.task.repository.GroupRepository;
import org.project.task.service.GroupService;
import org.project.task.service.GroupUserService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupUserService groupUserService;

    private final GroupRepository groupRepository;

    private final MapperGroup mapperGroup;

    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Void> saveGroup(Mono<CreateGroupDto> createGroupDtoMono, Jwt jwt) {
        return createGroupDtoMono.map(createGroupDto ->
            mapperGroup.groupDtoToGroup(createGroupDto, jwt))
                .flatMap(groupRepository::save).flatMap(group -> groupUserService.saveUserInGroup(jwt, group.getId(), "OWNER")
                ).as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Void> setGroup(Mono<SetGroupDto> setGroupDtoMono, Jwt jwt) {
        return setGroupDtoMono
                .flatMap(setGroupDto ->
                        groupUserService.verifyOwnerAccess(setGroupDto.id(), jwt).thenReturn(mapperGroup.createUpdateGroupFields(setGroupDto))
                                .flatMap(sqlIdentifierObjectMap -> groupRepository.updateFields(sqlIdentifierObjectMap, Group.class, "id", setGroupDto.id()))
                );
    }

    @Override
    public Mono<Void> delGroup(Long id, Jwt jwt) {
        return groupUserService.verifyOwnerAccess(id, jwt).then(groupRepository.deleteById(id));
    }

    @Override
    public Mono<List<GroupDto>> getGroups(Jwt jwt) {
        return groupRepository.findAllByUserId(UUID.fromString(jwt.getSubject())).collectList();
    }


}
