package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.dto.response.group.GroupDto;
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
    public Mono<Map<String, List<String>>> setTask(Mono<SetGroupDto> setGroupDtoMono, Jwt jwt) {
        return setGroupDtoMono.flatMap(setGroupDto -> verifyUserAccess(setGroupDto.id(), jwt).then(updateTaskFields(setGroupDto)));
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

    private Mono<Map<String, List<String>>> updateTaskFields(SetGroupDto setGroupDto){
        return groupRepository.findById(setGroupDto.id()).flatMap(group -> {
            List<String> updateFields = new ArrayList<>();
            if(setGroupDto.description() != null) {
                group.setDescription(setGroupDto.description());
                updateFields.add("description");
            }
            if(setGroupDto.name() != null) {
                group.setName(setGroupDto.name());
                updateFields.add("name");
            }
            if(updateFields.isEmpty()){
                return Mono.empty();
            }
            return groupRepository.save(group).thenReturn(
                    Map.of("update_fields", updateFields));
        });
    }
}
