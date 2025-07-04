package org.project.task.repository;

import org.project.task.entity.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface TaskReposiroty extends ReactiveCrudRepository<Task, Long> {

    Flux<Task> findByName(String name);
}
