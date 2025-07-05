package org.project.task.mapper.task;

import org.mapstruct.*;
import org.project.task.dto.request.CreateTaskDto;
import org.project.task.dto.response.TaskDto;
import org.project.task.entity.Task;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperTask {


    @Mapping(source = "deadLine", target = "deadLine", qualifiedByName = "millisToDeadlineDateTime")
    Task taskDtoToTask(CreateTaskDto createTaskDto, @Context Jwt jwt);

    @Mappings(value = {
            @Mapping(source = "createTime", target = "createTime", qualifiedByName = "timeToTimeZone"),
            @Mapping(source = "updateTime", target = "updateTime", qualifiedByName = "timeToTimeZone"),
            @Mapping(source = "deadLine", target = "deadLine", qualifiedByName = "timeToTimeZone")
    })
    TaskDto taskToTaskDto(Task task, @Context String timeZone);

    List<TaskDto> taskToTaskDto(List<Task> tasks);


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
    default void setCreateTimeAndUserID(@MappingTarget Task task, @Context Jwt jwt) {
        task.setCreateTime(LocalDateTime.now());
        task.setUserId(UUID.fromString(jwt.getSubject()));
    }

}
