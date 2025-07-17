package org.project.mapper.group;

import org.mapstruct.*;
import org.project.dto.request.group.CreateGroupDto;
import org.project.dto.request.group.SetGroupDto;
import org.project.entity.Group;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MapperGroup {

    Group groupDtoToGroup(CreateGroupDto createGroupDto, @Context Jwt jwt);


    default Map<SqlIdentifier, Object> createUpdateGroupFields(SetGroupDto setGroupDto){
        Map<SqlIdentifier, Object> updateMap = new HashMap<>();
        if(setGroupDto.description() != null) updateMap.put(SqlIdentifier.quoted("description"), setGroupDto.description());
        if(setGroupDto.name() != null) updateMap.put(SqlIdentifier.quoted("name"), setGroupDto.name());
        return updateMap;
    }
}
