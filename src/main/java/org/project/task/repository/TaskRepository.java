package org.project.task.repository;

import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.Task;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;



public interface TaskRepository extends ReactiveCrudRepository<Task, Long>, RepositoryUpdateFields{


    @Query("""
        select *
        from task t
        join group_tasks gt on t.id = gt.task_id
        where gt.group_id = $1
        order by t.dead_line
        """)
    Flux<TaskDto> findByGroupId(Long groupID);
}
