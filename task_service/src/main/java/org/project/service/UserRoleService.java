package org.project.service;


import reactor.core.publisher.Mono;

public interface UserRoleService {

    Mono<Long> findRoleIdByName(String name);
}
