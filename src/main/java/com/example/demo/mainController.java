package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.ResourceBundle;

public class mainController implements Initializable {

    private static final String DB_URL = "jdbc:sqlite:your_database_name.db";

    @FXML
    private CheckMenuItem hideUserMenuItem;


    @FXML
    private VBox postContainer1;

    @FXML
    private VBox groupContainer;
    @FXML
    private VBox friendsArea;
    @FXML
    private TextField searchField2;
    @FXML
    private TextField searchField;
    @FXML
    private Button publishButton;
    @FXML
    private TextArea postContentTextArea;
    @FXML
    private MenuItem item1;

    @FXML
    private AnchorPane leftPane;
    @FXML
    private Button postButton;
    @FXML
    private VBox postContainer;
    @FXML
    private MenuItem item2;
    @FXML
    private ListView<String> groupResultsView;
    @FXML
    private ListView<String> resultsListView;
    @FXML
    private SplitPane splitPane;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // postButton.layoutXProperty().bind(leftPane.layoutXProperty());
        // Load posts from the database
        loadFriendPostsFromDatabase();
        loadPostsFromDatabase();
        getFriendList(UserSession.getInstance().getUserId());
        displayUserGroups(UserSession.getInstance().getUserId());

    }

    public  int[] getFriendList(int userId) {
        wallController.initiliazable = 1;
        int[] friends = {};
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Step 1: Retrieve the friend list for the given user ID
            String query = "SELECT friend_list FROM users WHERE user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String friendList = resultSet.getString("friend_list");


                if (friendList != null && !friendList.isEmpty()) {
                    friends = convertStringToArray(friendList);




                } else {
                    System.out.println("User has no friends listed.");
                }



            } else {
                System.out.println("No user found with the user ID: " + userId);
            }

            for (int friendId : friends) {
                Label friendLabel = new Label("Friend ID: " + fetchUsernameById(conn,friendId));


                friendLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(javafx.scene.input.MouseEvent event) {
                        wallController.seenUser = friendId;
                        wallController.enableProfileWall = 2;
                        Parent home_page = null;
                        try {
                            home_page = FXMLLoader.load(getClass().getResource("othersWall.fxml"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Scene hp_scene = new Scene(home_page);
                        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        app_stage.setScene(hp_scene);
                        app_stage.show();

                    }
                });






                try {
                    friendsArea.getChildren().add(friendLabel);
                } catch (NullPointerException e) {
                    System.err.println("postContainer is null: " + e.getMessage());
                }
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
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

    @FXML
    private void handleSearch() {

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Update the search results dynamically as the user types
            String searchQuery = newValue.trim();
            if (searchQuery.isEmpty()) {
                resultsListView.setItems(FXCollections.observableArrayList());
                resultsListView.setVisible(false);
                return;
            }

            // Fetch search results from the database
            ObservableList<String> results = searchUsers(searchQuery);
            resultsListView.setVisible(true);
            resultsListView.setItems(results);
        });

        // Add click event handler to each item
        resultsListView.setOnMouseClicked(event -> handleResultClick(event));
    }

    private ObservableList<String> searchUsers(String query) {
        ObservableList<String> results = FXCollections.observableArrayList();
        String sql = "SELECT username FROM users WHERE username LIKE ? AND is_hidden = 0";


        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, "%" + query + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(resultSet.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while searching for users.");
        }

        return results;
    }


    private void handleResultClick(MouseEvent event) {
        String selectedUser = resultsListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Get user_id of the selected user
            int userId = getUserIdByUsername(selectedUser);
            if (userId != -1) {
                // Load the new page with selected user details
                try {
                    wallController.seenUser = userId;
                    wallController.enableProfileWall = 2;
                    Parent home_page = FXMLLoader.load(getClass().getResource("othersWall.fxml"));
                    Scene hp_scene = new Scene(home_page);
                    Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    app_stage.setScene(hp_scene);
                    app_stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to load the user profile page.");
                }
            } else {
                showAlert("User Not Found", "The selected user could not be found.");
            }
        }
    }

    public static int getUserIdByUsername(String username) {
        int userId = -1; // Default value indicating user not found
        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    userId = resultSet.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // showAlert("Database Error", "An error occurred while fetching the user ID.");
        }

        return userId;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }


    private void loadPostsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM posts ORDER BY post_id DESC";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Iterate over the result set and create post components
            while (resultSet.next()) {
                int user_Id = resultSet.getInt("post_owner");
                String content = resultSet.getString("text");

                // Fetch the username based on the userId
                String username = fetchUsernameById(conn, user_Id);

                // Create a post component
                TitledPane postComponent = createPostComponent(username, content);

                postComponent.getStyleClass().add("titled-pane-modern");

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

    @FXML
    private void logOut(ActionEvent event) throws IOException {
        Parent newPage = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene newPageScene = new Scene(newPage);

        // Get the current stage (window)
        Stage currentStage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

        // Set the new scene in the current stage
        currentStage.setScene(newPageScene);
        currentStage.setTitle("Login");
        currentStage.show();
    }


    private int[] getONLYFriendList(){


            int[] friends = {};
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                // Step 1: Retrieve the friend list for the given user ID
                String query = "SELECT friend_list FROM users WHERE user_id = ?";
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setInt(1, wallController.user.userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String friendList = resultSet.getString("friend_list");


                    if (friendList != null && !friendList.isEmpty()) {
                        friends = convertStringToArray(friendList);




                    } else {
                        System.out.println("User has no friends listed.");
                    }



                } else {
                    System.out.println("No user found with the user ID: " + wallController.user.userId);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
                }
            return friends;
    }

    private void loadFriendPostsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM posts ORDER BY post_id DESC";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Iterate over the result set and create post components
            while (resultSet.next()) {
                int user_Id = resultSet.getInt("post_owner");

                for(int a : getONLYFriendList()){
                    if(user_Id == a){
                        String content = resultSet.getString("text");

                        // Fetch the username based on the userId
                        String username = fetchUsernameById(conn, user_Id);

                        // Create a post component
                        TitledPane postComponent = createPostComponent(username, content);

                        // Add the post component to the postContainer
                        try {
                            postContainer1.getChildren().add(postComponent);
                        } catch (NullPointerException e) {
                            System.err.println("postContainer is null: " + e.getMessage());
                        }

                    }
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
    public void goTooProfile(ActionEvent event) throws IOException {
        wallController.enableProfileWall = 1;
        wallController.initiliazable = 0;
        Parent newPage = FXMLLoader.load(getClass().getResource("activeUserWall.fxml"));
        Scene newPageScene = new Scene(newPage);

        // Get the current stage (window)
        Stage currentStage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

        // Set the new scene in the current stage
        currentStage.setScene(newPageScene);
        currentStage.setTitle("My Profile");
        currentStage.show();
    }

    public void returnpFeed(ActionEvent event) throws IOException {
        wallController c = new wallController();
        c.enableProfileWall = 1;
        Parent ssPage = FXMLLoader.load(getClass().getResource("mainPage.fxml"));
        Scene sssPage = new Scene(ssPage);

        // Get the current stage (window)
        Stage currentStage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

        // Set the new scene in the current stage
        currentStage.setScene(sssPage);
        currentStage.setTitle("Feed");
        currentStage.show();
    }


    @FXML
    public void openPostWindow(ActionEvent event) throws IOException {
        Parent newPage = FXMLLoader.load(getClass().getResource("postWrite.fxml"));
        Scene newPageScene = new Scene(newPage);

        // Create a new stage for the new window
        Stage newStage = new Stage();
        newStage.setScene(newPageScene);
        newStage.setTitle("Post");
        newStage.show();
    }

    @FXML
    private void publishPost(ActionEvent event) throws IOException {
        int userID = UserSession.getInstance().getUserId();
        String postContent = postContentTextArea.getText();
        PostFactory.getPost(userID,postContent,-1);
        // Call method to write post data to the database
        writePostToDatabase(userID, postContent);

    }

    public static void writePostToDatabase(int userID, String postContent) {
        String insertPostSQL = "INSERT INTO posts (post_owner, text) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(insertPostSQL)) {

            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, postContent);

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


    @FXML
    private void handleGroupearch() {
        searchField2.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchQuery = newValue.trim();
            if (searchQuery.isEmpty()) {
                groupResultsView.setItems(FXCollections.observableArrayList());
                groupResultsView.setVisible(false);
            } else {
                // Fetch search results from the database
                ObservableList<String> results = searchGroups(searchQuery);
                groupResultsView.setItems(results);
                groupResultsView.setVisible(true);
            }
        });

        // Add click event handler to each item
        groupResultsView.setOnMouseClicked(this::group_handleResultClick);
    }

    private ObservableList<String> searchGroups(String query) {
        ObservableList<String> results = FXCollections.observableArrayList();
        String sql = "SELECT group_name FROM groups WHERE group_name LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, "%" + query + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(resultSet.getString("group_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while searching for groups.");
        }

        return results;
    }

    private void group_handleResultClick(MouseEvent event) {
        String selectedGroup = groupResultsView.getSelectionModel().getSelectedItem();
        if (selectedGroup != null) {
            // Get group_id of the selected group
            int groupId = getGroupIdByName(selectedGroup);
            groupController.selectedGroupId = groupId;
            if (groupId != -1) {
                // Load the new page with selected group details
                try {
                    // Assuming `groupController` is set up similarly to `wallController`
                    groupController.selectedGroupId = groupId;
                    Parent home_page = FXMLLoader.load(getClass().getResource("groupPage.fxml"));
                    Scene hp_scene = new Scene(home_page);
                    Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    app_stage.setScene(hp_scene);
                    app_stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to load the group page.");
                }
            } else {
                showAlert("Group Not Found", "The selected group could not be found.");
            }
        }
    }

    private int getGroupIdByName(String groupName) {
        int groupId = -1; // Default value indicating group not found
        String sql = "SELECT id FROM groups WHERE group_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, groupName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    groupId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while fetching the group ID.");
        }

        return groupId;
    }



    public void displayUserGroups(int userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT group_name, users_array, group_admins FROM groups";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String groupName = resultSet.getString("group_name");
                String usersArrayStr = resultSet.getString("users_array");
                System.out.println(usersArrayStr);
                String adminArrayStr = resultSet.getString("group_admins");

                if (!usersArrayStr.equals("[]")) {
                    String[] userIdsStr = usersArrayStr.isEmpty() ? new String[0] : usersArrayStr.replaceAll("[\\[\\]]", "").split(",");
                    int[] usersArray = new int[userIdsStr.length];
                    for (int i = 0; i < userIdsStr.length; i++) {
                        usersArray[i] = Integer.parseInt(userIdsStr[i].trim());
                    }
                    if (Arrays.stream(usersArray).anyMatch(id -> id == userId)) {
                        // Check if the group has users
                        if (usersArray.length > 0) {
                            Label groupLabel = new Label(groupName);
                            // groupLabel.getStyleClass().add("label-modern");


                            groupLabel.setOnMouseClicked(event -> {
                                int groupId = getGroupIdByName(groupName);
                                groupController.selectedGroupId = groupId;
                                try {
                                    Parent homePage = FXMLLoader.load(getClass().getResource("groupPage.fxml"));
                                    Scene hpScene = new Scene(homePage);
                                    Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                    appStage.setScene(hpScene);
                                    appStage.show();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            try {
                                groupContainer.getChildren().add(groupLabel);
                            } catch (NullPointerException e) {
                                System.err.println("groupContainer is null: " + e.getMessage());
                            }
                        }
                    }
                }

                if (adminArrayStr != null) {
                    String[] admIdStr = adminArrayStr.isEmpty() ? new String[0] : adminArrayStr.replaceAll("[\\[\\]]", "").split(",");
                    int[] adminArray = new int[admIdStr.length];
                    for (int i = 0; i < admIdStr.length; i++) {
                        adminArray[i] = Integer.parseInt(admIdStr[i]);
                    }

                    // Check if the user ID exists in the group_admins
                    if (Arrays.stream(adminArray).anyMatch(id -> id == userId)) {
                        // Check if the group has users
                        if (adminArray.length > 0) {
                            Label groupLabel = new Label(groupName);
                            groupLabel.setOnMouseClicked(event -> {
                                int groupId = getGroupIdByName(groupName);
                                groupController.selectedGroupId = groupId;
                                try {
                                    Parent homePage = FXMLLoader.load(getClass().getResource("groupPage.fxml"));
                                    Scene hpScene = new Scene(homePage);
                                    Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                    appStage.setScene(hpScene);
                                    appStage.show();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            try {
                                groupContainer.getChildren().add(groupLabel);
                            } catch (NullPointerException e) {
                                System.err.println("groupContainer is null: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private boolean isUserInArray(String usersArray, int userId) {
        // Assuming usersArray is a comma-separated string of user IDs
        String[] userIDs = usersArray.split(",");
        for (String id : userIDs) {
            if (id.trim().equals(String.valueOf(userId))) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void openCreateGroup(ActionEvent e) throws IOException {
        Parent newPage = FXMLLoader.load(getClass().getResource("createGroup.fxml"));
        Scene newPageScene = new Scene(newPage);

        // Create a new stage for the new window
        Stage newStage = new Stage();
        newStage.setScene(newPageScene);
        newStage.setTitle("Post");
        newStage.show();
    }


    @FXML
    private void handleHideUser() {
        boolean isHidden = hideUserMenuItem.isSelected();
        int hiddenValue = isHidden ? 1 : 0;

        // Assuming you want to hide a specific user, you might need their user ID
        int userId = wallController.user.userId; // Replace with actual user ID retrieval logic

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String updateQuery = "UPDATE users SET is_hidden = ? WHERE user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);
            preparedStatement.setInt(1, hiddenValue);
            preparedStatement.setInt(2, userId);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                String message = isHidden ? "User has been hidden from searches." : "User is now visible in searches.";
                showAlert("Update Successful", message);
            } else {
                showAlert("Update Failed", "User ID not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while updating the user status.");
        }
    }



}
