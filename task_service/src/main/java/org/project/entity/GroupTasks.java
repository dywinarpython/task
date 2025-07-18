package org.project.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "group_tasks")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GroupTasks {

    @Id
    private Long id;

    private Long groupId;

    private Long taskId;

    private UUID userId;

    private UUID assignBy;


}
