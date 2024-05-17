package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class mainController {

    @FXML
    private TextArea postContentTextArea;
    @FXML
    private MenuItem item1;

    @FXML
    private MenuItem item2;

    public void goToProfile(ActionEvent event) throws IOException {
        Parent newPage = FXMLLoader.load(getClass().getResource("wall.fxml"));
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

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:your_database_name.db");
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
