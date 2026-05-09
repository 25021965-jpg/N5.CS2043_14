package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    private static final String URL =
            "jdbc:mysql://localhost:3306/auction_system";

    private static final String USER = "root";

    private static final String PASSWORD = "Pass102938@";

    // KẾT NỐI DATABASE
    public static Connection getConnection() throws SQLException {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

        } catch (ClassNotFoundException e) {

            throw new SQLException(
                    "MySQL Driver not found!",
                    e
            );
        }
    }

    // KHỞI TẠO DATABASE
    public static void initDatabase() {

        String dbURL =
                "jdbc:mysql://localhost:3306/";

        try (
                Connection conn =
                        DriverManager.getConnection(
                                dbURL,
                                USER,
                                PASSWORD
                        );

                Statement stmt =
                        conn.createStatement()
        ) {

            // TẠO DATABASE
            stmt.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS myapp_db"
            );

            // CHUYỂN DATABASE
            stmt.executeUpdate(
                    "USE myapp_db"
            );

            // TẠO TABLE USERS
            String createUsersTable =
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT," +
                            "fullname VARCHAR(100) NOT NULL," +
                            "username VARCHAR(50) UNIQUE NOT NULL," +
                            "email VARCHAR(100) UNIQUE NOT NULL," +
                            "password VARCHAR(255) NOT NULL" +
                            ")";

            stmt.executeUpdate(createUsersTable);

            System.out.println(
                    "✓ Database initialized successfully!"
            );

        } catch (SQLException e) {

            System.err.println(
                    "Init database error: " +
                            e.getMessage()
            );
        }
    }
}