package org.project.websocket;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;



import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationsWebSocketHandler implements WebSocketHandler {

    private final ReactiveJwtDecoder jwtDecoder;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public NotificationsWebSocketHandler(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String token = extractJwtFromHeader(session);
        if (token == null) {
            return session.close(CloseStatus.NOT_ACCEPTABLE);
        }
        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    sessions.put(jwt.getSubject(), session);
                    return session.receive()
                            .doFinally(signal -> sessions.remove(jwt.getSubject()))
                            .then();
                })
                .onErrorResume(e -> {
                    log.warn("JWT decode error: {}", e.getMessage());
                    return session.close(CloseStatus.NOT_ACCEPTABLE);
                });
    }

    private String extractJwtFromHeader(WebSocketSession session) {
        HttpHeaders httpHeaders = session.getHandshakeInfo().getHeaders();
        String authorization = httpHeaders.getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        } else {
            log.warn("Заголовок Authorization отсутствует или некорректен");
        }
        return null;
    }

    public Mono<Void> sendMessageToUser(UUID userId, String message) {
        WebSocketSession session = sessions.get(userId.toString());
        if (session != null && session.isOpen()) {
            return session.send(Mono.just(session.textMessage(message)));
        }
        return Mono.empty();
    }
}

