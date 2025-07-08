package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.dto.response.group.GroupDto;
import org.project.task.entity.Group;
import org.project.task.mapper.group.MapperGroup;
import org.project.task.repository.GroupRepository;
import org.project.task.service.GroupService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    private final MapperGroup mapperGroup;

    @Override
    public Mono<Void> saveGroup(Mono<CreateGroupDto> createGroupDtoMono, Jwt jwt) {
        return createGroupDtoMono.map(createGroupDto ->
            mapperGroup.groupDtoToGroup(createGroupDto, jwt)).flatMap(groupRepository::save).then();
    }

    @Override
    public Mono<Void> setTask(Mono<SetGroupDto> setGroupDtoMono, Jwt jwt) {
        return setGroupDtoMono
                .flatMap(setGroupDto ->
                        verifyUserAccess(setGroupDto.id(), jwt).thenReturn(mapperGroup.createUpdateGroupFields(setGroupDto))
                                .flatMap(sqlIdentifierObjectMap -> groupRepository.updateFields(sqlIdentifierObjectMap, Group.class, "id", setGroupDto.id()))
                );
    }

    @Override
    public Mono<Void> delGroup(Long id, Jwt jwt) {
        return verifyUserAccess(id, jwt).then(groupRepository.deleteById(id));
    }

    @Override
    public Mono<List<GroupDto>> getGroups(Jwt jwt) {
        return groupRepository.findByUserID(UUID.fromString(jwt.getSubject())).collectList();
    }



    public Mono<Void> verifyUserAccess(Long groupId, Jwt jwt){
        return groupRepository.existsByIdAndUserID(groupId, UUID.fromString(jwt.getSubject()))
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return groupRepository.existsById(groupId)
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return Mono.error(new NoSuchElementException("Группа с id: " + groupId + " не найдена"));
                                    }
                                    return Mono.error(new AccessDeniedException("У вас нет прав управлять данной группой"));
                                });
                    }
                    return Mono.empty();
                });
    }


}
