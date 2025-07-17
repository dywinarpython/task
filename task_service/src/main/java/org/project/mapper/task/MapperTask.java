package org.project.mapper.task;

import org.mapstruct.*;
import org.project.dto.request.task.CreateTaskDto;
import org.project.dto.request.task.CreateTaskWithUserDto;
import org.project.dto.request.task.SetTaskDto;
import org.project.dto.response.task.TaskDto;
import org.project.dto.response.task.TaskWithUserDto;
import org.project.entity.Task;
import org.springframework.data.relational.core.sql.SqlIdentifier;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MapperTask {


    Task taskDtoToTask(CreateTaskDto createTaskDto);

    Task taskDtoToTask(CreateTaskWithUserDto createTaskWithUserDto);


    @Mappings(value = {
            @Mapping(source = "createTime", target = "createTime", qualifiedByName = "timeToTimeZone"),
            @Mapping(source = "updateTime", target = "updateTime", qualifiedByName = "timeToTimeZone"),
            @Mapping(source = "deadLine", target = "deadLine", qualifiedByName = "offsetToTimeZone")
    })
    TaskWithUserDto taskToTaskWithUserDto(TaskWithUserDto task, @Context String timeZone);


    @Mappings(value = {
            @Mapping(source = "createTime", target = "createTime", qualifiedByName = "timeToTimeZone"),
            @Mapping(source = "updateTime", target = "updateTime", qualifiedByName = "timeToTimeZone"),
            @Mapping(source = "deadLine", target = "deadLine", qualifiedByName = "offsetToTimeZone")
    })
    TaskDto taskDtoToTaskDto(TaskDto task, @Context String timeZone);

    List<TaskDto> taskListDtoTTaskListDto(List<TaskDto> list, @Context String timeZone);

    default Map<SqlIdentifier, Object> createUpdateTaskFields(SetTaskDto setTaskDto){
        Map<SqlIdentifier, Object> updateMap = new HashMap<>();
        if (setTaskDto.deadLine() != null) updateMap.put(SqlIdentifier.quoted("dead_line"), setTaskDto.deadLine());
        if(setTaskDto.description() != null) updateMap.put(SqlIdentifier.quoted("description"), setTaskDto.description());
        if(setTaskDto.name() != null) updateMap.put(SqlIdentifier.quoted("name"), setTaskDto.name());
        if(setTaskDto.status() != null) updateMap.put(SqlIdentifier.quoted("status"), setTaskDto.status());
        updateMap.put(SqlIdentifier.quoted("update_time"), LocalDateTime.now());
        return updateMap;
    }

    default Map<SqlIdentifier, Object> createCompleteTask(){
        Map<SqlIdentifier, Object> updateMap = new HashMap<>();
        updateMap.put(SqlIdentifier.quoted("update_time"), LocalDateTime.now());
        updateMap.put(SqlIdentifier.quoted("complete"), true);
        return updateMap;
    }


    @Named("timeToTimeZone")
    default LocalDateTime timeToTimeZone(LocalDateTime localDateTime, @Context String timeZone){
        if(localDateTime == null) return null;
        return localDateTime
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of(timeZone))
                .toLocalDateTime();

    }

    @Named("offsetToTimeZone")
    default OffsetDateTime offsetToTimeZone(OffsetDateTime offsetDateTime, @Context String timeZone){
        if(offsetDateTime == null) return null;
        return offsetDateTime.atZoneSameInstant(ZoneId.of(timeZone)).toOffsetDateTime();
    }

    @AfterMapping
    default void setCreateTime(@MappingTarget Task task) {
        task.setCreateTime(LocalDateTime.now());
    }

}
