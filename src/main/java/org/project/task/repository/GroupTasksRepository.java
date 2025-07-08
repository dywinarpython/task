package org.project.task.repository;

import org.project.task.entity.GroupTasks;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupTasksRepository extends ReactiveCrudRepository<GroupTasks, Long> {


    @Query("""
        select exists(
            select  1
            from group_tasks gt
            join "group" g on g.id = gt.group_id
            where gt.task_id = $1 and g.user_id = $2
        )
    """)
    Mono<Boolean> existsByTaskIDAndUserIdByGroupTasks(Long taskId, UUID userID);

}
