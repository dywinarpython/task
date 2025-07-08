package org.project.task.entity;

import jakarta.persistence.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "group_users")
public class GroupUsers {

    @Id
    private Long id;

    private UUID userId;

    private Long groupId;
}
