package org.project.task.repository;

import org.project.task.dto.response.task.TaskWithUserDto;
import org.project.task.entity.Task;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface TaskRepository extends ReactiveCrudRepository<Task, Long>, RepositoryUpdateFields{


    @Query("""
            SELECT
                  t.id,
                  t.name,
                  t.description,
                  t.dead_line,
                  t.create_time,
                  t.update_time,
                  t.status,
                  t.complete,
                  gt.user_id    AS assignee,
                  CASE
                    WHEN gt.user_id IS NULL THEN TRUE
                    WHEN gu.user_id IS NOT NULL THEN TRUE
                    ELSE FALSE
                  END AS is_active_user
                FROM task t
                  JOIN group_tasks gt
                    ON t.id = gt.task_id
                  LEFT JOIN group_users gu
                    ON gu.group_id = gt.group_id
                   AND gu.user_id = gt.user_id
                WHERE gt.group_id = :groupId
                ORDER BY t.dead_line
        """)
    Flux<TaskWithUserDto> findByGroupId(@Param("groupId") Long groupId);

    Flux<Task> findByName(String name);

    @Modifying
    @Query("delete from task where id = :id returning id")
    Flux<Long> deleteByIdReturning(@Param("id") Long id);

    @Modifying
    @Query("""
            delete from task
            where id in (
            	select g.task_id
            	from group_tasks g
            	where g.group_id = :groupId
            )
            """)
    Mono<Void> deleteTaskByGroupId(@Param("groupId") Long groupId);

}
