package org.project.task.repository;

import org.project.task.entity.GroupUsers;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupUsersRepository extends ReactiveCrudRepository<GroupUsers, Long> {
    Mono<Boolean> existsByGroupIdAndUserId(Long groupId, UUID userId);

    @Query(
            """
            select group_id
            from group_users g
            where g.user_id = $1
            """
    )
    Flux<Long> findGroupsIdByUserID(UUID userID);
}
