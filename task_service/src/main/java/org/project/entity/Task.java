package org.project.entity;


import org.springframework.data.annotation.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

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

    private OffsetDateTime deadLine;

    private LocalDateTime updateTime;

    private String status;

    private Boolean complete;
}
