package com.attendance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/absents_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Surya@123"; // Change to your MySQL password

    private static Connection connection = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                String msg = "MySQL JDBC Driver not found.\n" +
                        "Please ensure the MySQL Connector/J jar (e.g. mysql-connector-j-8.4.0.jar)\n" +
                        "is placed in the project's lib/ folder and included on the classpath.\n" +
                        "Example (Windows CMD):\n" +
                        "  java -cp \"out;lib\\mysql-connector-j-8.4.0.jar\" com.attendance.Main\n" +
                        "Or run via IDE by adding the jar to project libraries.\n\n" +
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
