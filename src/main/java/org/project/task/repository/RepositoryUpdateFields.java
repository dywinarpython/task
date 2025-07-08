package org.project.task.repository;

import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

import java.util.Map;

@FunctionalInterface
public interface RepositoryUpdateFields {

    <T> Mono<Void> updateFields(Map<SqlIdentifier, Object> sqlIdentifierObjectMap, Class<T> classT, String column, Object columnValue);
}
