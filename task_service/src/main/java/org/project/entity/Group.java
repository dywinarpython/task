package org.project.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "\"group\"")
@Getter
@Setter
@NoArgsConstructor
public class Group {

    @Id
    private Long id;

    private String name;

    private String description;



}
