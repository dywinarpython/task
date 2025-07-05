package org.project.task.repository;

import org.project.task.entity.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface TaskReposiroty extends ReactiveCrudRepository<Task, Long> {

    Flux<Task> findByName(String name);

    Mono<Boolean> existsByIdAndUserId(Long id, UUID userId);

    Flux<Task> findTaskByUserId(UUID userId);
}
