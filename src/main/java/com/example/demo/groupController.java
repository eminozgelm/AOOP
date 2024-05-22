package com.example.demo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.sql.*;
import java.util.*;

import static com.example.demo.Dbase.connect;
import static com.example.demo.Dbase.insertUser;

public class groupController implements Initializable {


    @FXML
    private Button removeUserButton = new Button();

    @FXML
    private Button adminAdd;

    @FXML
    private Button finish;

    private List<Integer> usersArray;
    private List<Integer> adminsArray;

    @FXML
    private Button userAdd;
    @FXML
    private TextField groupName;

    @FXML
    private ListView<String> resultsView;

    @FXML
    private TextField searchField;


    @FXML
    private VBox membersArea;
    @FXML
    private VBox postContainer;
    static int selectedGroupId;
    @FXML
    private TextArea postContentTextArea;
    @FXML
    private VBox admins;
    private static final String DB_URL = "jdbc:sqlite:your_database_name.db";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // postButton.layoutXProperty().bind(leftPane.layoutXProperty());
        // Load posts from the database
        loadPostsFromDatabase();
        getGroupMembersList(selectedGroupId, true);
        setAdmins();
        createNewArrays();
        handleRemoveButton();
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

        if (isGroupMember(userId, getGroupMembersList(groupId, false))) {
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

    public boolean isGroupMember(int userId, int[] usersArray) {
        for (int id : usersArray) {
            if (id == userId) {
                return true;
            }
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

    public int[] getGroupMembersList(int groupId, boolean initiliazable) {
        int[] groupMembers = {};
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Step 1: Retrieve the usersArray for the given group ID
            String query = "SELECT users_array FROM groups WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, groupId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String usersArray = resultSet.getString("users_array");

                if (usersArray != null && !usersArray.isEmpty()) {
                    groupMembers = convertStringToArray(usersArray);
                } else {
                    System.out.println("Group has no members listed.");
                }

                if(!initiliazable){
                    return groupMembers;
                }

                for (int memberId : groupMembers) {
                    Label memberLabel = new Label("Member Name: " + fetchUsernameById(conn, memberId));
                    try {
                        membersArea.getChildren().add(memberLabel);
                    } catch (NullPointerException e) {
                        System.err.println("postContainer is null: " + e.getMessage());
                    }
                }
            }

            // Fetch and display usernames for each member ID



        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupMembers;
    }



    private void loadPostsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT * FROM group_posts WHERE group_id = ? ORDER BY post_id DESC";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, selectedGroupId);
            ResultSet resultSet = statement.executeQuery();





            // Iterate over the result set and create post components
            while (resultSet.next()) {
                int postID = resultSet.getInt("post_id");
                int user_Id = resultSet.getInt("owner_id");
                String username = fetchUsernameById(conn, user_Id);
                String content = resultSet.getString("text");



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
                return new Group(name, usersArray, groupAdmins, selectedGroupId);
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

            preparedStatement.setInt(1, post.getGroupID());
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
    public void setAdmins(){
        int[] admin_ids = {};
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT group_admins FROM groups WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, selectedGroupId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String adminName = resultSet.getString("group_admins");
                admin_ids = convertStringToArray(adminName);
                for (int admin : admin_ids) {
                    Label adminLabel = new Label( fetchUsernameById(conn,admin));
                    try {
                        admins.getChildren().add(adminLabel);
                    } catch (NullPointerException e) {
                        System.err.println("postContainer is null: " + e.getMessage());
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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



    public static String convertArrayListToString(List<Integer> arrayList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < arrayList.size(); i++) {
            stringBuilder.append(arrayList.get(i));
            if (i < arrayList.size() - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }


    @FXML
    private void finish(){
        if(adminsArray == null) {
            adminsArray.add(wallController.user.userId);
        }
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String gp = groupName.getText();

            String insertQuery = "INSERT INTO groups (group_name, users_array, group_admins) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);
            preparedStatement.setString(1, gp);
            preparedStatement.setString(2, convertArrayListToString(usersArray));
            preparedStatement.setString(3, convertArrayListToString(adminsArray));
            preparedStatement.executeUpdate();
            preparedStatement.close();
            showAlert("Group Created", "The group has been successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    private void createNewArrays(){
        usersArray = new ArrayList<>();
        adminsArray = new ArrayList<>();
    }

    @FXML
    public void handleSearch(){
        setupSearch(searchField, resultsView);
    }

    @FXML
    public void buttonHandle(ActionEvent ev){
        userAdd.setOnAction(event -> {
            String selectedUser = searchField.getText();
            if (!selectedUser.isEmpty()) {
                int userId = mainController.getUserIdByUsername(selectedUser);
                if (userId == -1) {
                    showAlert("User not found", "User " + selectedUser + " does not exist.");
                } else if (usersArray.contains(userId)) {
                    showAlert("User already added", "User " + selectedUser + " is already added to the group.");
                } else {
                    usersArray.add(userId);
                    showAlert("User added", "User " + selectedUser + " has been added to the group.");
                }
            }
        });

        adminAdd.setOnAction(event -> {
            String selectedAdmin = searchField.getText();
            if (!selectedAdmin.isEmpty()) {
                int adminId = mainController.getUserIdByUsername(selectedAdmin);
                if (adminId == -1) {
                    showAlert("User not found", "User " + selectedAdmin + " does not exist.");
                } else if (adminsArray.contains(adminId)) {
                    showAlert("Admin already added", "User " + selectedAdmin + " is already added as an admin.");
                } else {
                    adminsArray.add(adminId);
                    showAlert("Admin added", "User " + selectedAdmin + " has been added as an admin.");
                }
            }
        });
    }

    public List<String> fetchUsernames(String searchTerm) {
        List<String> usernames = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT username FROM users WHERE username LIKE ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, "%" + searchTerm + "%");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                usernames.add(resultSet.getString("username"));
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usernames;
    }

    // Set up search functionality
    public void setupSearch(TextField searchField, ListView<String> searchResults) {
        ObservableList<String> suggestions = FXCollections.observableArrayList();
        searchResults.setItems(suggestions);

        searchField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            String searchTerm = searchField.getText();
            if (searchTerm.length() >= 2) { // Start suggesting after 2 characters
                List<String> matches = fetchUsernames(searchTerm);
                suggestions.setAll(matches);
            } else {
                suggestions.clear();
            }
        });

        searchResults.setOnMouseClicked(event -> {
            String selectedUser = searchResults.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                searchField.setText(selectedUser);
                searchResults.getItems().clear();
            }
        });
    }


    @FXML
    public void handleRemoveButton(){
        boolean x = isAdmin();
        removeUserButton.setVisible(x);

        removeUserButton.setOnAction(event -> {
            if (isAdmin()) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Remove User");
                dialog.setHeaderText("Enter the username to remove:");
                dialog.setContentText("Username:");

                // Show remove user dialog and get the input
                String usernameToRemove = dialog.showAndWait().orElse(null);
                if (usernameToRemove != null && !usernameToRemove.isEmpty()) {
                    int userIdToRemove = mainController.getUserIdByUsername(usernameToRemove);
                    if (userIdToRemove != -1) {
                        try (Connection conn = DriverManager.getConnection(DB_URL)) {
                            // Get the current users_array value
                            String selectQuery = "SELECT users_array FROM groups WHERE id = ?";
                            PreparedStatement selectStatement = conn.prepareStatement(selectQuery);
                            selectStatement.setInt(1, selectedGroupId);
                            ResultSet resultSet = selectStatement.executeQuery();

                            if (resultSet.next()) {
                                String currentUsersArray = resultSet.getString("users_array");
                                // Remove the userId from the users_array
                                String updatedUsersArray = currentUsersArray
                                        .replaceAll("\\[" + userIdToRemove + ",", "") // Remove if at the beginning
                                        .replaceAll("," + userIdToRemove + ",", ",") // Remove if in the middle
                                        .replaceAll("," + userIdToRemove + "\\]", "]"); // Remove if at the end

                                // Update the users_array in the group_database table
                                String updateQuery = "UPDATE groups SET users_array = ? WHERE id = ?";
                                PreparedStatement updateStatement = conn.prepareStatement(updateQuery);
                                updateStatement.setString(1, updatedUsersArray);
                                updateStatement.setInt(2, selectedGroupId);
                                updateStatement.executeUpdate();

                                System.out.println("User deleted from the group successfully.");
                            }

                            resultSet.close();
                            selectStatement.close();

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    // Update database or perform any other necessary action
                    showAlert("User Removed", "User " + usernameToRemove + " has been removed from the group.");
                } else {
                    showAlert("User Not Found", "User " + usernameToRemove + " is not in the group.");
                }
            }  else {
                showAlert("Access Denied", "You do not have permission to remove users from the group.");
            }

        });
    }

    private boolean isAdmin() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT group_admins FROM groups WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, selectedGroupId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String adminIdsString = resultSet.getString("group_admins");
                int[] adminIds = convertStringToArray(adminIdsString);
                for (int adminId : adminIds) {
                    System.out.println(wallController.user.userId);
                    System.out.println(adminId);
                    if (adminId == wallController.user.userId) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }


}