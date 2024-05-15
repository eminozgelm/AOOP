package com.example.demo;

import java.sql.*;
import java.util.List;


import java.sql.*;
import java.util.List;
import com.google.gson.Gson;

public class Dbase {

    // Establishing a connection to the SQLite database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:your_database_name.db");
            System.out.println("Connection to SQLite database successful.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return conn;
    }

    // Creating the users table if it doesn't exist
    public static void createUsersTable(Connection conn) {
        try {
            Statement statement = conn.createStatement();
            String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INTEGER PRIMARY KEY,"
                    + "username TEXT NOT NULL UNIQUE,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "password_hash TEXT NOT NULL,"
                    + "first_name TEXT,"
                    + "last_name TEXT,"
                    + "profile_info TEXT,"
                    + "friend_list TEXT)";
            statement.executeUpdate(createUserTableSQL);
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Inserting a user into the users table
    public static void insertUser(Connection conn, String username, String email, String passwordHash,
                                  String firstName, String lastName, String profileInfo, List<Integer> friends) {
        try {
            String friendListJson = new Gson().toJson(friends); // Convert friend list to JSON
            String insertUserSQL = "INSERT INTO users (username, email, password_hash, first_name, last_name, profile_info, friend_list) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertUserSQL);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, passwordHash);
            preparedStatement.setString(4, firstName);
            preparedStatement.setString(5, lastName);
            preparedStatement.setString(6, profileInfo);
            preparedStatement.setString(7, friendListJson);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            System.out.println("User inserted successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Removing a user from the users table by username
    public static void removeUser(Connection conn, String username) {
        try {
            String removeUserSQL = "DELETE FROM users WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(removeUserSQL);
            preparedStatement.setString(1, username);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User removed successfully.");
            } else {
                System.out.println("No user found with username: " + username);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Connection conn = connect();
        if (conn != null) {
            // Step 1: Create the users table if it doesn't exist
            //createUsersTable(conn);

            // Step 2: Insert sample users with friend connections
            insertUser(conn, "user1", "user1@example.com", "password_hash_1", "John", "Doe", "Software Engineer", List.of(2, 3));
            insertUser(conn, "user2", "user2@example.com", "password_hash_2", "Jane", "Smith", "Data Scientist", List.of(1, 3));
            insertUser(conn, "user3", "user3@example.com", "password_hash_3", "Alice", "Johnson", "Graphic Designer", List.of(1, 2));
            insertUser(conn, "user4", "user4@example.com", "password_hash_4", "Bob", "Johnson", "Accountant", List.of(5));
            insertUser(conn, "user5", "user5@example.com", "password_hash_5", "Emily", "Brown", "Teacher", List.of(4));

            // Step 3: Close the connection
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}



