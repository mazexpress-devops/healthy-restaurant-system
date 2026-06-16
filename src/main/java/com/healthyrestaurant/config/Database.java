package com.healthyrestaurant.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/healthy_restaurant"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                    + "&useUnicode=true&characterEncoding=UTF-8";
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final Properties PROPERTIES = loadProperties();

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String url = value("db.url", "DB_URL", DEFAULT_URL);
        String user = value("db.user", "DB_USER", "root");
        String password = value("db.password", "DB_PASSWORD", "");
        loadDriver();
        return DriverManager.getConnection(url, user, password);
    }

    private static void loadDriver() throws SQLException {
        try {
            Class.forName(MYSQL_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver was not found: " + MYSQL_DRIVER, e);
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path externalConfig = Paths.get("config", "db.properties");

        if (Files.exists(externalConfig)) {
            try (InputStream input = Files.newInputStream(externalConfig)) {
                properties.load(input);
                return properties;
            } catch (IOException ignored) {
                // Fall back to classpath/default values.
            }
        }

        try (InputStream input = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ignored) {
            // Fall back to default values.
        }
        return properties;
    }

    private static String value(String propertyKey, String environmentKey, String defaultValue) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.trim().isEmpty()) {
            return environmentValue.trim();
        }
        return PROPERTIES.getProperty(propertyKey, defaultValue).trim();
    }
}
