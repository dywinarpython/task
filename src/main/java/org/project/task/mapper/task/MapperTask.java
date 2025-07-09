package org.project.task.mapper.task;

import org.mapstruct.*;
import org.project.task.dto.request.task.CreateTaskDto;
import org.project.task.dto.request.task.CreateTaskWithUserDto;
import org.project.task.dto.request.task.SetTaskDto;
import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.Task;
import org.springframework.data.relational.core.sql.SqlIdentifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MapperTask {


    @Mapping(source = "deadLine", target = "deadLine", qualifiedByName = "millisToDeadlineDateTime")
    Task taskDtoToTask(CreateTaskDto createTaskDto);

    @Mapping(source = "deadLine", target = "deadLine", qualifiedByName = "millisToDeadlineDateTime")
    Task taskDtoToTask(CreateTaskWithUserDto createTaskWithUserDto);

    @Mappings(value = {
            @Mapping(source = "createTime", target = "createTime", qualifiedByName = "timeToTimeZone"),
            @Mapping(source = "updateTime", target = "updateTime", qualifiedByName = "timeToTimeZone"),
            @Mapping(source = "deadLine", target = "deadLine", qualifiedByName = "timeToTimeZone")
    })
    TaskDto taskToTaskDto(TaskDto taskDto, @Context String timeZone);




    default Map<SqlIdentifier, Object> createUpdateTaskFields(SetTaskDto setTaskDto){
        Map<SqlIdentifier, Object> updateMap = new HashMap<>();
        if (setTaskDto.deadLine() != null) updateMap.put(SqlIdentifier.quoted("dead_line"), LocalDateTime.now().plus(Duration.ofMillis(setTaskDto.deadLine())));
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

    @Named("millisToDeadlineDateTime")
    default LocalDateTime millisToDeadlineDateTime(Long millis){
        if(millis == null) return null;
        return LocalDateTime.now().plus(Duration.ofMillis(millis));
    }

    @Named("timeToTimeZone")
    default LocalDateTime timeToTimeZone(LocalDateTime localDateTime, @Context String timeZone){
        if(localDateTime == null) return null;
        return localDateTime
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of(timeZone))
                .toLocalDateTime();

    }

    @AfterMapping
    default void setCreateTimeAndUserID(@MappingTarget Task task) {
        task.setCreateTime(LocalDateTime.now());
    }

}
