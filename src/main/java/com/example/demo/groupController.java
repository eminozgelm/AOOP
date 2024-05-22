package com.example.demo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static com.example.demo.Dbase.connect;
import static com.example.demo.Dbase.insertUser;

public class groupController implements Initializable {

    @FXML
    private VBox membersArea;
    @FXML
    private VBox postContainer;
    static int selectedGroupId;
    @FXML
    private TextArea postContentTextArea;
    private static final String DB_URL = "jdbc:sqlite:your_database_name.db";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // postButton.layoutXProperty().bind(leftPane.layoutXProperty());
        // Load posts from the database
        loadPostsFromDatabase();
        getGroupMembersList(1);

    }
    public void returnFeed(ActionEvent event) throws IOException {
        Parent newPage = FXMLLoader.load(getClass().getResource("mainPage.fxml"));
        Scene newPageScene = new Scene(newPage);

        // Get the current stage (window)
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Set the new scene in the current stage
        currentStage.setScene(newPageScene);
        currentStage.setTitle("Feed");
        currentStage.show();
    }
    @FXML
    public void openPostWindow(ActionEvent event) throws IOException {

        int userId = UserSession.getInstance().userId;
        int groupId = selectedGroupId; // Implement this to get the current group ID

        if (isUserGroupMember(userId, groupId)) {
            Parent newPage = FXMLLoader.load(getClass().getResource("groupPostWrite.fxml"));
            Scene newPageScene = new Scene(newPage);

            // Create a new stage for the new window
            Stage newStage = new Stage();
            newStage.setScene(newPageScene);
            newStage.setTitle("Post");
            newStage.show();
        } else {
            showAlert("Access Denied", "You are not a member of this group and cannot post content.");
        }
    }

    private boolean isUserGroupMember(int userId, int groupId) {
        String sql = "SELECT users_array FROM groups WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String usersArray = resultSet.getString("users_array");
                    // Assuming users_array is stored as a comma-separated string of user IDs
                    List<String> userList = Arrays.asList(usersArray.split(","));
                    return userList.contains(String.valueOf(userId));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while checking group membership.");
        }
        return false;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }


    @FXML
    private void publishPost(ActionEvent event) throws IOException {
        int userID = UserSession.getInstance().getUserId();
        String postContent = postContentTextArea.getText();
        // Call method to write post data to the database
        writePostToDatabase(1,userID, postContent);

    }

    private static void writePostToDatabase(int groupID,int userID, String postContent) {
        String insertPostSQL = "INSERT INTO group_posts (group_id, owner_id, text) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(insertPostSQL)) {

            preparedStatement.setInt(1, groupID);
            preparedStatement.setInt(2, userID);
            preparedStatement.setString(3, postContent);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Post published successfully.");
            } else {
                System.out.println("Failed to publish post.");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public int[] getGroupMembersList(int groupId) {
        int[] groupMembers = {};
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Step 1: Retrieve the usersArray for the given group ID
            String query = "SELECT users_array FROM groups WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, groupId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String usersArray = resultSet.getString("users_array");

                if (usersArray != null && !usersArray.isEmpty()) {
                    groupMembers = convertStringToArray(usersArray);
                } else {
                    System.out.println("Group has no members listed.");
                }
            } else {
                System.out.println("No group found with the ID: " + groupId);
            }

            // Fetch and display usernames for each member ID
            for (int memberId : groupMembers) {
                Label memberLabel = new Label("Member Name: " + fetchUsernameById(conn, memberId));
                membersArea.getChildren().add(memberLabel);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupMembers;
    }
    public static int[] convertStringToArray(String input) {
        // Remove square brackets
        input = input.replaceAll("\\[", "").replaceAll("\\]", "");

        // Split the string by commas
        String[] stringArray = input.split(",");

        // Convert the string array to an int array
        int[] intArray = new int[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            intArray[i] = Integer.parseInt(stringArray[i].trim());
        }

        return intArray;
    }

    private void loadPostsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM group_posts ORDER BY post_id DESC";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Iterate over the result set and create post components
            while (resultSet.next()) {
                int user_Id = resultSet.getInt("owner_id");
                String content = resultSet.getString("text");

                // Fetch the username based on the userId
                String username = fetchUsernameById(conn, user_Id);

                // Create a post component
                TitledPane postComponent = createPostComponent(username, content);

                // Add the post component to the postContainer
                try {
                    postContainer.getChildren().add(postComponent);
                } catch (NullPointerException e) {
                    System.err.println("postContainer is null: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String fetchUsernameById(Connection conn, int userId) {
        String username = "";
        String query = "SELECT username FROM users WHERE user_id = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                username = resultSet.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return username;
    }

    private TitledPane createPostComponent(String username, String content) {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(username);

        TextArea contentArea = new TextArea(content);
        contentArea.setWrapText(true); // Enable text wrapping
        contentArea.setEditable(false); // Make the text area non-editable

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(contentArea);

        AnchorPane.setTopAnchor(contentArea, 0.0);
        AnchorPane.setBottomAnchor(contentArea, 0.0);
        AnchorPane.setLeftAnchor(contentArea, 0.0);
        AnchorPane.setRightAnchor(contentArea, 0.0);

        titledPane.setContent(anchorPane);

        return titledPane;
    }


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
