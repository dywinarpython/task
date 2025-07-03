package org.project.task.repository;

import org.project.task.entity.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TaskReposiroty extends ReactiveCrudRepository<Task, Long> {
}
