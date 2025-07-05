package org.project.task.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.project.task.dto.request.CreateTaskDto;
import org.project.task.dto.request.SetTaskDto;
import org.project.task.dto.response.TaskDto;
import org.project.task.entity.Task;
import org.project.task.mapper.task.MapperTask;
import org.project.task.repository.TaskReposiroty;
import org.project.task.service.TaskService;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.naming.AuthenticationException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskReposiroty taskReposiroty;

    private final MapperTask mapperTask;


    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<List<TaskDto>> getTask(String timeZone, Jwt jwt) {
        return taskReposiroty.findTaskByUserId(UUID.fromString(jwt.getSubject())).map(task -> mapperTask.taskToTaskDto(task, timeZone)).collectList();
    }

    @Override
    public Mono<Void> saveTask(Mono<CreateTaskDto> createTaskDto, Jwt jwt) {

        return createTaskDto.map(dto -> mapperTask.taskDtoToTask(dto, jwt))
                .flatMap(taskReposiroty::save)
                .then();
    }

    @Override
    public Mono<Map<String, List<String>>> setTask(Mono<SetTaskDto> setTaskDtoMono, Jwt jwt) {
        return setTaskDtoMono.flatMap(setTaskDto -> this.updateTaskFields(setTaskDto, jwt));
    }

    @Override
    public Mono<Void> delTask(Long id, Jwt jwt) {
        UUID userID = UUID.fromString(jwt.getSubject());
        return taskReposiroty.existsByIdAndUserId(id, userID).flatMap(bl -> {
            if(!bl){
                return taskReposiroty.existsById(id).flatMap( ex -> {
                            if (!ex) {
                                return Mono.error(new NoSuchElementException("Элемент с id: " + id + " не найден"));
                            }
                            return Mono.error(new AuthenticationException("У вас нет доступа удалять данную задачу"));
                        }
                );
            }
            return taskReposiroty.deleteById(id);
        });
    }


    private Mono<Map<String, List<String>>> updateTaskFields(SetTaskDto setTaskDto, Jwt jwt){
        Map<SqlIdentifier, Object> updateMap = new HashMap<>();
        if (setTaskDto.deadLine() != null) updateMap.put(SqlIdentifier.quoted("dead_line"), LocalDateTime.now().plus(Duration.ofMillis(setTaskDto.deadLine())));
        if(setTaskDto.description() != null) updateMap.put(SqlIdentifier.quoted("description"), setTaskDto.description());
        if(setTaskDto.name() != null) updateMap.put(SqlIdentifier.quoted("name"), setTaskDto.name());
        if(setTaskDto.status() != null) updateMap.put(SqlIdentifier.quoted("status"), setTaskDto.status());
        if(updateMap.isEmpty()){
            return Mono.empty();
        }
        updateMap.put(SqlIdentifier.quoted("update_time"), LocalDateTime.now());

        Update update = Update.from(updateMap);

        UUID userID = UUID.fromString(jwt.getSubject());
        return r2dbcEntityTemplate.update(
                Query.query(
                        Criteria.where("id").is(setTaskDto.id()).and(Criteria.where("user_id").is(userID))), update, Task.class)
                .flatMap(count -> {
                    if (count == 0){
                        return taskReposiroty.existsById(setTaskDto.id()).flatMap(bl -> {
                            if (!bl) {
                                return Mono.error(new NoSuchElementException("Элемента с id: " + setTaskDto.id() + " нет."));
                            }
                            return Mono.error(new AuthenticationException("У вас нет доступа изменять данную задачу"));
                        });
                    } return  Mono.just(count);
                })
                .thenReturn(Map.of("update", updateMap.keySet().stream().map(SqlIdentifier::getReference).toList()));
    }
}
