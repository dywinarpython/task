package org.project.task.repository;

import org.project.task.entity.UserRole;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {

}
