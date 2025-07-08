package org.project.task.entity;


import org.springframework.data.annotation.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    private Long id;

    private String name;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime deadLine;

    private LocalDateTime updateTime;

    private String status;
}
