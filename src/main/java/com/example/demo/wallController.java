package com.example.demo;

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

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class wallController implements Initializable {
    private static final String DB_URL = "jdbc:sqlite:your_database_name.db";
    @FXML
    private Label activeUserName;
    @FXML
    private Label profileName;

    private boolean isEditable = false;
    @FXML
    private TextArea bio;

    @FXML
    private Button changeBioButton;

    static UserSession user;

    static int seenUser;

    static int enableProfileWall;
    @FXML
    private MenuItem item1;

    @FXML
    private MenuItem item2;

    @FXML
    private VBox postContainer;

    public wallController(){
    }

    public wallController(UserSession userX){
        user = userX;


    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // postButton.layoutXProperty().bind(leftPane.layoutXProperty());
        // Load posts from the database
        loadPostsFromDatabase();
        loadUserBio();
    }


    @FXML
    private void handleEditButton() {
        if (isEditable) {
            // Save the text to the database
            changeBio(bio.getText());
            bio.setEditable(false);
            changeBioButton.setText("Change Bio");
        } else {
            bio.setEditable(true);
            changeBioButton.setText("Save");
        }
        isEditable = !isEditable;
    }
    @FXML
    private void changeBio(String profileInfo)
    {
        String query = "UPDATE users SET profile_info = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {

            preparedStatement.setString(1, profileInfo);

            if(enableProfileWall == 1) {
                preparedStatement.setInt(2, user.userId);
            }else {
                preparedStatement.setInt(2, seenUser);
            }


            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile info updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile info.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating profile info.");
        }
        return;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadUserBio(){

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT username, first_name, last_name, profile_info FROM users WHERE user_id = ?";

            // Create a PreparedStatement with the parameterized query
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                // Set the user_id parameter
                if(enableProfileWall == 1) {
                    preparedStatement.setInt(1, user.userId);
                }else {
                    preparedStatement.setInt(1, seenUser);
                }

                // Execute the query
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    // Process the result set
                    while (resultSet.next()) {
                        activeUserName.setText(resultSet.getString("username"));
                        profileName.setText(resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                        bio.setText(resultSet.getString("profile_info"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
    private void loadPostsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM posts WHERE post_owner = ? ORDER BY post_id DESC";
            PreparedStatement statement = conn.prepareStatement(query);
            System.out.println(enableProfileWall);
            if(enableProfileWall == 1) {
                statement.setInt(1, user.userId);
            }else {
                System.out.println("IM HERE");
                statement.setInt(1, seenUser);
            }
            ResultSet resultSet = statement.executeQuery();

            // Iterate over the result set and create post components
            while (resultSet.next()) {
                String content = resultSet.getString("text");

                String username;
                // Fetch the username based on the userId
                if(enableProfileWall == 1){
                    username = fetchUsernameById(conn, user.userId);
                }else {
                    username = fetchUsernameById(conn, seenUser);
                }


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
            if(enableProfileWall == 1) {
                preparedStatement.setInt(1, user.userId);
            }else {
                preparedStatement.setInt(1, seenUser);
            }
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
    public void goToProfile(ActionEvent event) throws IOException {
        Parent newPage = FXMLLoader.load(getClass().getResource("activeUserWall.fxml"));
        Scene newPageScene = new Scene(newPage);

        // Get the current stage (window)
        Stage currentStage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

        // Set the new scene in the current stage
        currentStage.setScene(newPageScene);
        currentStage.setTitle("My Profile");
        currentStage.show();;
    }

    public void returnFeed(ActionEvent event) throws IOException {
        Parent newPage = FXMLLoader.load(getClass().getResource("mainPage.fxml"));
        Scene newPageScene = new Scene(newPage);

        // Get the current stage (window)
        Stage currentStage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

        // Set the new scene in the current stage
        currentStage.setScene(newPageScene);
        currentStage.setTitle("Feed");
        currentStage.show();;
    }




}
