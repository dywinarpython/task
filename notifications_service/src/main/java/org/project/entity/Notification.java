package org.project.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private UUID userId;
    private String message;
    private boolean read;
    private LocalDateTime localDateTime;

}