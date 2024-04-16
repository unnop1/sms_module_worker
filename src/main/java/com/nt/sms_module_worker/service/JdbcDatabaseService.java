package com.nt.sms_module_worker.service;
// Java Program Illustrating Utility class for Connecting
// and Querying the Databas

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

// Annotation to provide logging feature

// Class
@Component
public class JdbcDatabaseService {

    @Value("${spring.datasource.url}")
    private String connectionUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private static HikariDataSource dataSource;

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public JdbcDatabaseService(@Value("${spring.datasource.url}") String jdbcUrl,
                               @Value("${spring.datasource.username}") String jdbcUsername,
                               @Value("${spring.datasource.password}") String jdbcPassword) {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);

        // Additional HikariCP configurations
        config.setMaximumPoolSize(10); // Maximum number of connections in the pool
        config.setMinimumIdle(5); // Minimum number of idle connections in the pool
        config.setConnectionTimeout(30000); // Timeout for establishing a new connection
        config.setIdleTimeout(600000); // Maximum time that a connection is allowed to remain idle
        config.setMaxLifetime(1800000); // Maximum lifetime of a connection in the pool

        // Add a connection test query to check if the connection is still valid
        config.setConnectionTestQuery("SELECT 1 FROM dual");

        // Add other necessary configurations here

        dataSource = new HikariDataSource(config);
    }
}


