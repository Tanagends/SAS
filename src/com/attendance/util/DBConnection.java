package com.attendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_SQLITE_URL = "jdbc:sqlite:absents.db?foreign_keys=on";
    private static final String DEFAULT_MYSQL_URL = "jdbc:mysql://localhost:3306/absents_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // resolved at startup from environment or system properties
    private static final String URL = determineUrl();
    private static final String USER = determineUser();
    // Read password from environment variable SAS_DB_PASS or system property 'db.pass' to avoid hardcoding credentials.
    private static final String PASS = determinePassword();

    // NOTE: Do NOT cache a single Connection instance. Many DAOs use try-with-resources
    // and will close the Connection — returning a shared Connection leads to "database
    // has been closed" / "stmt pointer is closed" errors. Create a new Connection for
    // each call and let the caller close it.

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
        String driver = determineDriver();
        try {
            if (driver != null) {
                Class.forName(driver);
            }
            // Always create a fresh connection for the caller.
            if (USER != null && !USER.isEmpty() && !URL.startsWith("jdbc:sqlite")) {
                return DriverManager.getConnection(URL, USER, PASS);
            } else {
                return DriverManager.getConnection(URL);
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

    // Keep a no-op close helper to avoid breaking callers; actual connections are closed by callers.
    public static void closeConnection() {
        // no-op because connections are not cached
    }
}
