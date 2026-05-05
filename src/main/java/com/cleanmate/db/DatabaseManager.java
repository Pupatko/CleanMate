package com.cleanmate.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

public final class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());
    private static HikariDataSource dataSource;

    private DatabaseManager() {}

    public static void init() {
        try (InputStream in = DatabaseManager.class
                .getClassLoader().getResourceAsStream("database.properties")) {

            Properties props = new Properties();
            props.load(in);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size", "10")));
            config.setConnectionTimeout(30_000);
            config.setIdleTimeout(600_000);
            config.setMaxLifetime(1_800_000);

            dataSource = new HikariDataSource(config);
            LOG.info("Database connection pool initialized.");

            runScript("db/schema.sql");
            runScript("db/migrate_availability.sql");
            runScript("db/migrate_password.sql");

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) throw new IllegalStateException("DatabaseManager not initialized.");
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOG.info("Database connection pool closed.");
        }
    }

    private static void runScript(String resourcePath) {
        try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) { LOG.warning("SQL script not found: " + resourcePath); return; }
            String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(java.util.stream.Collectors.joining("\n"));
            try (Connection con = dataSource.getConnection(); Statement st = con.createStatement()) {
                for (String stmt : sql.split(";")) {
                    String s = stmt.strip();
                    if (!s.isEmpty() && !s.startsWith("--")) st.execute(s);
                }
            }
            LOG.info("Executed script: " + resourcePath);
        } catch (Exception e) {
            LOG.severe("Failed to run script " + resourcePath + ": " + e.getMessage());
        }
    }
}
