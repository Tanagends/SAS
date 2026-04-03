package com.attendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // sensible defaults
    private static final String DEFAULT_SQLITE_URL = "jdbc:sqlite:absents.db?foreign_keys=on";
    private static final String DEFAULT_MYSQL_URL = "jdbc:mysql://localhost:3306/absents_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // resolved at startup from environment or system properties
    private static final String URL = determineUrl();
    private static final String USER = determineUser();
    // Read password from environment variable SAS_DB_PASS or system property 'db.pass' to avoid hardcoding credentials.
    private static final String PASS = determinePassword();

    private static Connection connection = null;

    private static String determineUrl() {
        String env = System.getenv("SAS_DB_URL");
        if (env != null && !env.isEmpty()) return env;
        String prop = System.getProperty("db.url");
        if (prop != null && !prop.isEmpty()) return prop;
        // default to local SQLite file for portability in team demos
        return DEFAULT_SQLITE_URL;
    }

    private static String determineUser() {
        String env = System.getenv("SAS_DB_USER");
        if (env != null && !env.isEmpty()) return env;
        String prop = System.getProperty("db.user");
        if (prop != null && !prop.isEmpty()) return prop;
        return "java_admin";
    }

    private static String determinePassword() {
        String env = System.getenv("SAS_DB_PASS");
        if (env != null && !env.isEmpty()) return env;
        String prop = System.getProperty("db.pass");
        if (prop != null && !prop.isEmpty()) return prop;
        // default to the working credential provided by the user
        return "admin";
    }

    private static String determineDriver() {
        String env = System.getenv("SAS_DB_DRIVER");
        if (env != null && !env.isEmpty()) return env;
        String prop = System.getProperty("db.driver");
        if (prop != null && !prop.isEmpty()) return prop;
        if (URL.startsWith("jdbc:sqlite")) return "org.sqlite.JDBC";
        if (URL.startsWith("jdbc:mysql")) return "com.mysql.cj.jdbc.Driver";
        return null;
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String driver = determineDriver();
            try {
                if (driver != null) {
                    Class.forName(driver);
                }
                // If USER is supplied use the (url,user,pass) form, otherwise use url-only (for sqlite)
                if (USER != null && !USER.isEmpty() && !URL.startsWith("jdbc:sqlite")) {
                    connection = DriverManager.getConnection(URL, USER, PASS);
                } else {
                    connection = DriverManager.getConnection(URL);
                }
            } catch (ClassNotFoundException e) {
                String msg = "JDBC Driver not found: " + (determineDriver() == null ? "(unknown)" : determineDriver()) + "\n" +
                        "Please add the appropriate JDBC driver jar to lib/ and include it on the classpath.\n" +
                        "Examples:\n" +
                        "  - MySQL: place mysql-connector-j-<version>.jar in lib/\n" +
                        "  - SQLite: place sqlite-jdbc-<version>.jar (org.xerial) in lib/\n\n" +
                        "Original error: " + e.getMessage();
                try {
                    javax.swing.JOptionPane.showMessageDialog(null, msg, "JDBC Driver Missing",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                } catch (Throwable t) {
                    // ignore UI errors when running in headless environments
                }
                throw new SQLException(msg, e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
