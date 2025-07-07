package org.project.task.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "\"group\"")
@Getter
@Setter
@NoArgsConstructor
public class Group {

    @Id
    private Long id;

    private String name;

    private String description;

    private UUID userID;

}
