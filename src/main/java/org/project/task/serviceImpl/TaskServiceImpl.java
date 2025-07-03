package org.project.task.serviceImpl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.project.task.dto.CreateTaskDto;
import org.project.task.dto.SetTaskDto;
import org.project.task.dto.TaskDto;
import org.project.task.entity.Task;
import org.project.task.mapper.task.MapperTask;
import org.project.task.repository.TaskReposiroty;
import org.project.task.service.TaskService;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskReposiroty taskReposiroty;

    private final MapperTask mapperTask;


    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<List<TaskDto>> getTask() {
        return taskReposiroty.findAll().map(mapperTask::taskToTaskDto).collectList();
    }

    @Override
    public Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDto) {

        return createTaskDto.map(dto -> mapperTask.taskDtoToTask(dto, ZoneId.of(dto.zoneID())))
                .flatMap(taskReposiroty::save)
                .then();
    }

    @Override
    public Mono<Map<String, List<String>>> setTask(Mono<SetTaskDto> setTaskDtoMono) {
        return setTaskDtoMono.flatMap(this::updateTaskFields);
    }

    @Override
    public Mono<Void> delTask(Long id) {
        return taskReposiroty.existsById(id).flatMap(bl -> {
            if(!bl){
                return Mono.error(new NoSuchElementException("Элемента с id: " + id + " нет"));
            }
            return taskReposiroty.deleteById(id);
        });
    }


    private Mono<Map<String, List<String>>> updateTaskFields(SetTaskDto setTaskDto){
        Map<SqlIdentifier, Object> updateMap = new HashMap<>();
        ZoneId zoneId = ZoneId.of(setTaskDto.zoneID());
        if (setTaskDto.deadLine() != null) updateMap.put(SqlIdentifier.quoted("dead_line"), LocalDateTime.now(zoneId).plus(Duration.ofMillis(setTaskDto.deadLine())));
        if(setTaskDto.description() != null) updateMap.put(SqlIdentifier.quoted("description"), setTaskDto.description());
        if(setTaskDto.name() != null) updateMap.put(SqlIdentifier.quoted("name"), setTaskDto.name());
        if(setTaskDto.status() != null) updateMap.put(SqlIdentifier.quoted("status"), setTaskDto.status());
        if(updateMap.isEmpty()){
            return Mono.empty();
        }
        updateMap.put(SqlIdentifier.quoted("create_time"), LocalDateTime.now(zoneId));

        Update update = Update.from(updateMap);

        return r2dbcEntityTemplate.update(Query.query(Criteria.where("id").is(setTaskDto.id())), update, Task.class)
                .flatMap(count -> {
                    if (count == 0){
                        return Mono.error(new NoSuchElementException("Элемента с id: " + setTaskDto.id() + ", нет."));
                    } return  Mono.just(count);
                })
                .thenReturn(Map.of("update", updateMap.keySet().stream().map(SqlIdentifier::getReference).toList()));
    }
}
