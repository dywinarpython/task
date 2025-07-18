package org.project.repository;

import org.project.dto.response.task.TaskDto;
import org.project.entity.GroupTasks;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface GroupTasksRepository extends ReactiveCrudRepository<GroupTasks, Long> {

    Mono<Boolean> existsByTaskId(Long taskId);

    @Query("""
            select group_id
            from group_tasks g
            where g.task_id = :taskId
    """)
    Mono<Long> findGroupIdByTaskId(@Param("taskId") Long taskId);

    @Query("""
            select t.id,
                  t.name,
                  t.description,
                  t.dead_line,
                  t.create_time,
                  t.update_time,
                  t.status,
                  t.complete,
                  g.assign_by
             from group_tasks g
             join task t on t.id = g.task_id
             where (g.user_id IS NULL or g.user_id = :userId)
             and g.group_id = :groupId
             LIMIT :limit OFFSET :offset
    """)
    Flux<TaskDto> findTaskForUserWithGroupID(@Param("userId") UUID userId, @Param("groupId") Long groupId,  @Param("limit") long limit,
                                             @Param("offset") long offset);

    @Query("""
    select exists(
        select 1
        from group_tasks gt
        join group_users gs on gt.group_id = gs.group_id
        where gs.user_id = :userId
             and gt.task_id = :taskId
    )
""")
    Mono<Boolean> checkingWhetherUserIsPerformingThisTask(
            @Param("userId") UUID userId,
            @Param("taskId") Long taskId
    );

}
