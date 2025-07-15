package org.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "roles")
public class UserRole {

    @Id
    private Long id;

    private String name;
}
