package org.project.repository;

import org.project.dto.response.group.GroupDto;
import org.project.entity.Group;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface GroupRepository extends ReactiveCrudRepository<Group, Long>, RepositoryUpdateFields {
    @Query("""
    select g.*
    from "group" g
    join group_users gu ON g.id = gu.group_id
    where gu.user_id = :userId
    LIMIT :limit OFFSET :offset
    """)
    Flux<GroupDto> findAllByUserId(
            @Param("userId") UUID userId,
            @Param("limit") long limit,
            @Param("offset") long offset
    );


    Flux<Group> findByName(String name);

    @Query("""
            select name
            from "group"
            where id = :groupId
            """)
    Mono<String> getNameGroupById(@Param("groupId") Long groupId);


}
