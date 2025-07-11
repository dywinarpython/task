package org.project.task.repository;

import org.project.task.dto.response.group.GroupDto;
import org.project.task.entity.Group;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;


public interface GroupRepository extends ReactiveCrudRepository<Group, Long>, RepositoryUpdateFields {

    @Query("""
            select g.*
            from "group" g
            join group_users gu on g.id = gu.group_id
            where gu.user_id = :userID
            """)
    Flux<GroupDto> findAllByUserId(@Param("userId") UUID userID);

    Flux<Group> findByName(String name);

}
