package com.example.demo;

import com.google.gson.Gson;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Dbase {
    // Static list to hold users
    private static List<User> usersList = new ArrayList<>();

    // Establishing a connection to the SQLite database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:your_database_name.db");
            System.out.println("Connection to SQLite database successful.");
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
        return conn;
    }

    public static int authenticateUser(Connection conn, String username, String password) {
        int userId = -1;
        try {
            String selectUserSQL = "SELECT user_id, password_hash FROM users WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(selectUserSQL);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPasswordHash = resultSet.getString("password_hash");
                // You should use a secure password verification method here, like bcrypt
                // For simplicity, this example uses direct comparison (not recommended for production)
                if (storedPasswordHash.equals(password)) {
                    userId = resultSet.getInt("user_id");
                    System.out.println("User authenticated successfully. User ID: " + userId);
                } else {
                    System.out.println("Authentication failed: Incorrect password.");
                }
            } else {
                System.out.println("Authentication failed: User not found.");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return userId;
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
                    + "friend_list TEXT,"
                    + "is_hidden INTEGER)";
            statement.executeUpdate(createUserTableSQL);
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }

    // Inserting a user into the users table
    public static void insertUser(Connection conn, String username, String email, String passwordHash,
                                  String firstName, String lastName, String profileInfo, List<Integer> friends, int isHidden) {
        try {
            User user = new User(username, email, passwordHash, firstName, lastName, profileInfo.split(","), friends.stream().mapToInt(i -> i).toArray(),isHidden);
            String friendListJson = new Gson().toJson(friends);
            String profileInfosJson = new Gson().toJson(profileInfo);// Convert friend list to JSON
            String insertUserSQL = "INSERT INTO users (username, email, password_hash, first_name, last_name, profile_info, friend_list, is_hidden) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertUserSQL);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmailAdress());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getFirstName());
            preparedStatement.setString(5, user.getLastName());
            preparedStatement.setString(6, profileInfosJson);
            preparedStatement.setString(7, friendListJson);
            preparedStatement.setInt(8, user.getIsHidden());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            System.out.println("User inserted successfully.");

            // Add user to the usersList
            usersList.add(user);
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
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
                // Remove user from usersList
                usersList.removeIf(user -> user.getUsername().equals(username));
            } else {
                System.out.println("No user found with username: " + username);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error removing user: " + e.getMessage());
        }
    }

    public static List<User> getUsersList() {
        return usersList;
    }

    public static void main(String[] args) {
        Connection conn = connect();
        if (conn != null) {
            // Step 1: Create the users table if it doesn't exist
            createUsersTable(conn);

            // Step 2:
            // Insert sample users with friend connections
            insertUser(conn, "user1", "user1@example.com", "password_hash_1", "John", "Doe", "Software Engineer", List.of(2, 3), 0);
            insertUser(conn, "user2", "user2@example.com", "password_hash_2", "Jane", "Smith", "Data Scientist", List.of(1, 3), 0);
            insertUser(conn, "user3", "user3@example.com", "password_hash_3", "Alice", "Johnson", "Graphic Designer", List.of(1, 2), 0);
            insertUser(conn, "user4", "user4@example.com", "password_hash_4", "Bob", "Johnson", "Accountant", List.of(5), 0);
            insertUser(conn, "user5", "user5@example.com", "password_hash_5", "Emily", "Brown", "Teacher", List.of(4), 1);
            insertUser(conn, "xdaaaa", "xdaaaa", "xdaaaa", "xdaaaa", "xdaaaa", "xdaaaa", List.of(4), 0);

            // Step 3: Close the connection
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}