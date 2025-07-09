package org.project.task.repository;

import org.project.task.entity.GroupUsers;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
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
            where g.user_id = :userId
            """
    )
    Flux<Long> findGroupsIdByUserID(@Param("userId") UUID userID);

    Mono<Boolean> existsByGroupId(Long groupId);

    @Query("""
    SELECT EXISTS(
        SELECT 1
        FROM group_users
        WHERE user_id = :userId AND group_id = :groupId AND role_id = ANY(:roles)
    )
    """)
    Mono<Boolean> existsByRoles(@Param("userId") UUID userId, @Param("groupId") Long groupId, @Param("roles") Long[] id);


}
