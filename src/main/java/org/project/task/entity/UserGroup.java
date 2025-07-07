package org.project.task.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "user_group")
@Getter
@Setter
@NoArgsConstructor
public class UserGroup {

    @Id
    private Long id;

    private UUID userId;

    private Group group;
}
