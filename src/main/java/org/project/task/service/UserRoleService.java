package org.project.task.service;


import reactor.core.publisher.Mono;

public interface UserRoleService {

    Mono<Long> findRoleIdByName(String name);
}
