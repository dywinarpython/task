package org.project.task.repository;

import org.project.task.entity.GroupUsers;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupUsersRepository extends ReactiveCrudRepository<GroupUsers, Long>{
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
    select exists(
        select 1
        from group_users
        where user_id = :userId AND group_id = :groupId AND role_id = ANY(:roles)
    )
    """)
    Mono<Boolean> existsByRoles(@Param("userId") UUID userId, @Param("groupId") Long groupId, @Param("roles") Long[] id);

    @Modifying
    @Query("""
            update group_users
            set role_id = :roleId
            where group_id = :groupId and user_id = :userId
            """)
    Mono<Long> assigningRights(@Param("groupId") Long groupId, @Param("userId") UUID userId, @Param("roleId") Long roleId);

    @Modifying
    @Query("""
            delete from group_users
            where group_id = :groupId and user_id = :userId and role_id != :roleId
            """)
    Mono<Long> deleteByUserIDAndByGroupId(@Param("groupId") Long groupId, @Param("userId") UUID userId, @Param("roleId") Long roleId);

    @Query("""
            select user_id
            from group_users
            where group_id = :groupId
            """)
    Flux<UUID> findUserIDByGroupId(@Param("groupId") Long groupId);


}
