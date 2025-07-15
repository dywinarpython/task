package org.project.config;

import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@Configuration
public class TestConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLContainer<?> postgreSQLContainer(){
        return new PostgreSQLContainer<>("postgres:17.5")
                .withDatabaseName("taskDb");
    }
    @Bean
    public DataSource dataSource(PostgreSQLContainer<?> postgreSQLContainer){
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        hikariDataSource.setUsername(postgreSQLContainer.getUsername());
        hikariDataSource.setPassword(postgreSQLContainer.getPassword());
        return hikariDataSource;
    }

    @Bean
    public ConnectionFactory connectionFactory(PostgreSQLContainer<?> postgreSQLContainer) {
        PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
                .host(postgreSQLContainer.getHost())
                .port(postgreSQLContainer.getFirstMappedPort())
                .database(postgreSQLContainer.getDatabaseName())
                .username(postgreSQLContainer.getUsername())
                .password(postgreSQLContainer.getPassword())
                .build();

        return new ConnectionPool(ConnectionPoolConfiguration.builder(new PostgresqlConnectionFactory(config))
                .build());
    }
}
