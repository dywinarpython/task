package org.project.task.mapper.task;

import org.mapstruct.*;
import org.project.task.dto.CreateTaskDto;
import org.project.task.dto.TaskDto;
import org.project.task.entity.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MapperTask {

    @Mappings({
            @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.ofInstant(java.time.Instant.now(), zoneId))"),
            @Mapping(source = "deadLine", target = "deadLine", qualifiedByName = "millisToDeadlineDateTime")
    })
    Task taskDtoToTask(CreateTaskDto createTaskDto, @Context ZoneId zoneId);

    TaskDto taskToTaskDto(Task task);

    List<TaskDto> taskToTaskDto(List<Task> tasks);



    @Named("millisToDeadlineDateTime")
    static LocalDateTime millisToDeadlineDateTime(Long millis, @Context ZoneId zoneId){
        if(millis == null) return null;
        return LocalDateTime.now(zoneId).plus(Duration.ofMillis(millis));
    }
}
