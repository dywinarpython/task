package org.project.task.mapper.group;

import org.mapstruct.*;
import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.entity.Group;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperGroup {

    Group groupDtoToGroup(CreateGroupDto createGroupDto, @Context Jwt jwt);

    @AfterMapping
    default void addUserID(@MappingTarget Group group, @Context Jwt jwt){
        group.setUserID(UUID.fromString(jwt.getSubject()));
    }
}
