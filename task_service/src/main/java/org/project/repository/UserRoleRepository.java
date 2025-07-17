package org.project.repository;

import org.project.entity.UserRole;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {

}
