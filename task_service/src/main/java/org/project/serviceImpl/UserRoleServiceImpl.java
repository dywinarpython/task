package org.project.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.entity.UserRole;
import org.project.repository.UserRoleRepository;
import org.project.service.UserRoleService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private Map<String, Long> roleNameToId;

    @EventListener(ApplicationReadyEvent.class)
    private void init() {
        userRoleRepository.findAll()
                .collectMap(UserRole::getName, UserRole::getId)
                .doOnNext(map -> roleNameToId = map)
                .subscribe();
    }

    public Mono<Long> findRoleIdByName(String name) {
        Long id = roleNameToId.get(name);
        if(id == null){
            log.error("Возникла критическая ошибка в поиске ролей, а именно роль с имененм {} не найдена", name);
            return Mono.error(new RuntimeException("Role with name: " + name + "not found"));
        }
        return Mono.just(id);
    }
}
