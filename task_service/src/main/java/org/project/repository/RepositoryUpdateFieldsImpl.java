package org.project.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RepositoryUpdateFieldsImpl implements RepositoryUpdateFields {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;


    @Override
    public <T> Mono<Void> updateFields(Map<SqlIdentifier, Object> sqlIdentifierObjectMap, Class<T> classT, String column, Object columnValue) {
        Update update = Update.from(sqlIdentifierObjectMap);
        return r2dbcEntityTemplate.update(Query.query(Criteria.where(column).is(columnValue)), update, classT).then();
    }
}
