package org.project.task.repository;

import org.project.task.dto.response.task.TaskDto;
import org.project.task.entity.GroupTasks;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface GroupTasksRepository extends ReactiveCrudRepository<GroupTasks, Long> {

    @Query("""
            select group_id
            from group_tasks g
            where g.task_id = :taskId
    """)
    Mono<Long> findGroupIdByTaskId(@Param("taskId") Long taskId);

    @Query("""
            select t.*
             from group_tasks g
             join task t on t.id = g.task_id
             where (g.user_id IS NULL or g.user_id = :userId)
             and g.group_id = :groupId
    """)
    Flux<TaskDto> findTaskForUserWithGroupID(@Param("userId") UUID userId, @Param("groupId") Long groupId);

    @Query("""
            select exists(
                select 1
                from group_tasks g
                where (g.user_id IS NULL or g.user_id = :userId)
                and g.group_id = :groupId and g.task_id = :taskId)
    """)
    Mono<Boolean> checkingWhetherUserIsPerformingThisTask(@Param("userId") UUID userId, @Param("groupId") Long groupId, @Param("taskId") Long taskId);

}
