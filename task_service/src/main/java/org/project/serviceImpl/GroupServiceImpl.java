package org.project.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.dto.request.group.CreateGroupDto;
import org.project.dto.request.group.SetGroupDto;
import org.project.dto.response.group.ListGroupDto;
import org.project.entity.Group;
import org.project.mapper.group.MapperGroup;
import org.project.repository.GroupRepository;
import org.project.service.GroupService;
import org.project.service.GroupUserService;
import org.project.service.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    @Value("${pageable.size}")
    private Long size;

    private final GroupUserService groupUserService;

    private final TaskService taskService;

    private final GroupRepository groupRepository;


    private final MapperGroup mapperGroup;

    private final TransactionalOperator transactionalOperator;

    @CacheEvict(value = "GROUPS_ID", key = "#jwt.getSubject()")
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
        return groupUserService.verifyOwnerAccess(id, jwt)
                .then(taskService.delAllTaskByGroupId(id))
                .then(groupRepository.deleteById(id))
                .as(transactionalOperator::transactional);
    }


    @Cacheable(value = "GROUPS", key = "#jwt.getSubject() + ' ' + #page")
    @Override
    public Mono<ListGroupDto> getGroups(Jwt jwt, Long page) {
        return groupRepository.findAllByUserId(UUID.fromString(jwt.getSubject()), size, page*size ).collectList().map(ListGroupDto::new);
    }


}
