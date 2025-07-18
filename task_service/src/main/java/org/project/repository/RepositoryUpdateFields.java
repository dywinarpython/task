package org.project.repository;

import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface RepositoryUpdateFields {

    <T> Mono<Void> updateFields(Map<SqlIdentifier, Object> sqlIdentifierObjectMap, Class<T> classT, String column, Object columnValue);
}
