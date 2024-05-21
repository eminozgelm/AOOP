package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.util.ResourceBundle;

public class mainController implements Initializable {

    private static final String DB_URL = "jdbc:sqlite:your_database_name.db";

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
    private ListView<String> resultsListView;
    @FXML
    private SplitPane splitPane;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // postButton.layoutXProperty().bind(leftPane.layoutXProperty());
        // Load posts from the database
        loadPostsFromDatabase();
    }



    @FXML
    private void handleSearch() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            resultsListView.setItems(FXCollections.observableArrayList());
            return;
        }

        // Fetch search results from the database
        ObservableList<String> results = searchUsers(searchQuery);
        resultsListView.setItems(results);

        // Add click event handler to each item
        resultsListView.setOnMouseClicked(event -> handleResultClick(event));
    }

    private ObservableList<String> searchUsers(String query) {
        ObservableList<String> results = FXCollections.observableArrayList();
        String sql = "SELECT username FROM users WHERE username LIKE ?";

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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("user_profile_view.fxml"));
                    Parent root = loader.load();

                    // Pass the user_id to the new controller
                    wallController controller = loader.getController();
                    controller.seenUser = (userId);

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("User Profile");
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to load the user profile page.");
                }
            } else {
                showAlert("User Not Found", "The selected user could not be found.");
            }
        }
    }

    private int getUserIdByUsername(String username) {
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
            showAlert("Database Error", "An error occurred while fetching the user ID.");
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
    public void goTooProfile(ActionEvent event) throws IOException {
        Parent newPage = FXMLLoader.load(getClass().getResource("activeUserWall.fxml"));
        Scene newPageScene = new Scene(newPage);

        // Get the current stage (window)
        Stage currentStage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

        // Set the new scene in the current stage
        currentStage.setScene(newPageScene);
        currentStage.setTitle("My Profile");
        currentStage.show();;
    }

    public void returnpFeed(ActionEvent event) throws IOException {
        Parent ssPage = FXMLLoader.load(getClass().getResource("mainPage.fxml"));
        Scene sssPage = new Scene(ssPage);

        // Get the current stage (window)
        Stage currentStage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

        // Set the new scene in the current stage
        currentStage.setScene(sssPage);
        currentStage.setTitle("Feed");
        currentStage.show();;
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
        // Call method to write post data to the database
        writePostToDatabase(userID, postContent);


    }

    private void writePostToDatabase(int userID, String postContent) {
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


}
