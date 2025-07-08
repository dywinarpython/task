package org.project.task.repository;

import org.project.task.dto.response.group.GroupDto;
import org.project.task.entity.Group;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupRepository extends ReactiveCrudRepository<Group, Long>, RepositoryUpdateFields {


    Flux<GroupDto> findByUserID(UUID userID);



    Mono<Boolean> existsByIdAndUserID(Long id, UUID userID);

    Long id(Long id);
}
