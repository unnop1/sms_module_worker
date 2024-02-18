package com.nt.sms_module_worker.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Value;

public class JdbcConnector {

    @Value("${spring.datasource.url}")
    private static String databaseUrl;

    @Value("${spring.datasource.username}")
    private static String username;

    @Value("${spring.datasource.password}")
    private static String password;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, username, password);
    }

    public static ResultSet executeQuery(String query) throws SQLException {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            return stmt.executeQuery(query);
        }
    }
}
    