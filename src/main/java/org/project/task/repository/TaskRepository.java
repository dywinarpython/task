package org.project.task.repository;

import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.Task;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TaskRepository extends ReactiveCrudRepository<Task, Long>{


    Flux<Task> findByName(String name);


    Flux<TaskDto> findByGroupId(Long groupId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 
        FROM task t
        JOIN "group" p ON p.id = t.group_id
        WHERE t.id = $1 AND p.user_id = $2
    )
    """)
    Mono<Boolean> existsByIdAndUserIdWithTableGroup(Long id, UUID userID);


}
