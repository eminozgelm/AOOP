package com.example.demo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.Dbase.connect;
import static com.example.demo.Dbase.insertUser;

public class groupController {

    // Inserting a group into the groups table
    public static void insertGroup(Connection conn, Group group) {
        try {
            String usersArrayJson = new Gson().toJson(group.getUsersArray());
            String groupAdminsJson = new Gson().toJson(group.getGroupAdmins());
            String insertGroupSQL = "INSERT INTO groups (group_name, users_array, group_admins) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertGroupSQL);
            preparedStatement.setString(1, group.getGroupName());
            preparedStatement.setString(2, usersArrayJson);
            preparedStatement.setString(3, groupAdminsJson);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            System.out.println("Group inserted successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Removing a group from the groups table
    public static void removeGroup(Connection conn, String groupName) {
        try {
            String removeGroupSQL = "DELETE FROM groups WHERE group_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(removeGroupSQL);
            preparedStatement.setString(1, groupName);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Group removed successfully.");
            } else {
                System.out.println("No group found with name: " + groupName);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Fetching a group from the database
    public static Group fetchGroupByName(Connection conn, String groupName) {
        try {
            String fetchGroupSQL = "SELECT group_name, users_array, group_admins FROM groups WHERE group_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(fetchGroupSQL);
            preparedStatement.setString(1, groupName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("group_name");
                String usersArrayJson = resultSet.getString("users_array");
                String groupAdminsJson = resultSet.getString("group_admins");
                Type arrayType = new TypeToken<int[]>() {}.getType();
                int[] usersArray = new Gson().fromJson(usersArrayJson, arrayType);
                int[] groupAdmins = new Gson().fromJson(groupAdminsJson, arrayType);
                resultSet.close();
                preparedStatement.close();
                return new Group(name, usersArray, groupAdmins);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    // Update a user in a group (add or remove)
    public static void updateUserInGroup(Connection conn, String groupName, int userId, boolean add, int authenticatedUserId) {
        Group group = fetchGroupByName(conn, groupName);
        if (group != null) {
            int[] groupAdmins = group.getGroupAdmins();
            List<Integer> adminIdsList = new ArrayList<>();
            for (int id : groupAdmins) {
                adminIdsList.add(id);
            }

            // Check if the authenticated user is an admin of the group
            if (adminIdsList.contains(authenticatedUserId)) {
                int[] usersArray = group.getUsersArray();
                List<Integer> userIdsList = new ArrayList<>();
                for (int id : usersArray) {
                    userIdsList.add(id);
                }

                if (add) {
                    if (!userIdsList.contains(userId)) {
                        userIdsList.add(userId);
                    } else {
                        System.out.println("User is already a member of the group.");
                        return;
                    }
                } else {
                    if (userIdsList.contains(userId)) {
                        userIdsList.remove(Integer.valueOf(userId));
                    } else {
                        System.out.println("User is not a member of the group.");
                        return;
                    }
                }

                int[] updatedUsersArray = userIdsList.stream().mapToInt(i -> i).toArray();
                group.setUsersArray(updatedUsersArray);

                try {
                    String updateGroupSQL = "UPDATE groups SET users_array = ? WHERE group_name = ?";
                    PreparedStatement preparedStatement = conn.prepareStatement(updateGroupSQL);
                    preparedStatement.setString(1, new Gson().toJson(group.getUsersArray()));
                    preparedStatement.setString(2, group.getGroupName());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    System.out.println("Group updated successfully.");
                } catch (SQLException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Authenticated user is not authorized to update users in this group.");
            }
        } else {
            System.out.println("Group not found: " + groupName);
        }
    }

    // Inserting a post into the group_posts table
    public static void insertGroupPost(Connection conn, GroupPost post) {
        try {
            String insertPostSQL = "INSERT INTO group_posts (group_name, group_id, owner_id, text) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertPostSQL);

            preparedStatement.setString(1, post.getGroupName());
            preparedStatement.setInt(2, post.getOwnerId());
            preparedStatement.setString(3, post.getText());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            System.out.println("Post inserted successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Removing a post from the group_posts table
    public static void removeGroupPost(Connection conn, int postId) {
        try {
            String removePostSQL = "DELETE FROM group_posts WHERE post_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(removePostSQL);
            preparedStatement.setInt(1, postId);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Post removed successfully.");
            } else {
                System.out.println("No post found with ID: " + postId);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Assume user is authenticated and their ID is set in UserSession
        int authenticatedUserId = UserSession.getInstance().getUserId();

        Connection conn = connect();
        if (conn != null) {
            try {
                // Step 1: Create the necessary tables if they don't exist

                // Step 2: Insert sample users
                insertUser(conn, "user1", "user1@example.com", "password_hash_1", "John", "Doe", "Software Engineer", new ArrayList<>(),false);
                insertUser(conn, "user2", "user2@example.com", "password_hash_2", "Jane", "Smith", "Data Scientist", new ArrayList<>(),false);

                // Step 3: Insert a sample group
                Group engineersGroup = new Group("EngineersGroup", new int[]{1, 2}, new int[]{authenticatedUserId}); // Group with user IDs 1 and 2, and admin with authenticated user ID
                Group csGroup = new Group("CSGroup", new int[]{1, 2}, new int[]{authenticatedUserId}); // Group with user IDs 1 and 2, and admin with authenticated user ID
                insertGroup(conn, engineersGroup);
                insertGroup(conn, csGroup);

                // Step 4: Update group users (add user with ID 3 to the EngineersGroup)
                updateUserInGroup(conn, "EngineersGroup", 3, true, authenticatedUserId); // Add user with ID 3 to the EngineersGroup

                // Step 5: Update group users (remove user with ID 1 from the CSGroup)
                updateUserInGroup(conn, "CSGroup", 1, false, authenticatedUserId); // Remove user with ID 1 from the CSGroup

                // Step 6: Create group posts
                GroupPost post1 = new GroupPost(engineersGroup.getGroupName(), authenticatedUserId, "Hello Engineers!");
                GroupPost post2 = new GroupPost(csGroup.getGroupName(), authenticatedUserId, "Hello CS!"); //GROUPPOSTUN GROUP ID OLARAK DEGIL ISIM OLARAK AL !!!!!!!!


                // Step 7: Insert group posts into the database
                insertGroupPost(conn, post1);
                insertGroupPost(conn, post2);

                // Step 8: Close the connection
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

}
