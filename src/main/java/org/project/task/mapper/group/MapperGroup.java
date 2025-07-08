package org.project.task.mapper.group;

import org.mapstruct.*;
import org.project.task.dto.request.group.CreateGroupDto;
import org.project.task.dto.request.group.SetGroupDto;
import org.project.task.entity.Group;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperGroup {

    Group groupDtoToGroup(CreateGroupDto createGroupDto, @Context Jwt jwt);

    @AfterMapping
    default void addUserID(@MappingTarget Group group, @Context Jwt jwt){
        group.setUserID(UUID.fromString(jwt.getSubject()));
    }
    default Map<SqlIdentifier, Object> createUpdateGroupFields(SetGroupDto setGroupDto){
        Map<SqlIdentifier, Object> updateMap = new HashMap<>();
        if(setGroupDto.description() != null) updateMap.put(SqlIdentifier.quoted("description"), setGroupDto.description());
        if(setGroupDto.name() != null) updateMap.put(SqlIdentifier.quoted("name"), setGroupDto.name());
        return updateMap;
    }
}
