package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ResourceBundle;
import java.io.IOException;
import java.net.URL;

public class mainController {

    @FXML
    private MenuItem item1;

    @FXML
    private MenuItem item2;

    @FXML
    private VBox postContainer;

    @FXML
    private ScrollPane scrollPane;

    public void initialize(URL location, ResourceBundle resources) {
        // Set VBox as content of ScrollPane
        scrollPane.setContent(postContainer);

        // Load initial posts (for demonstration)
        loadPosts();
    }

    private void loadPosts() {
        // Simulated data: add some sample posts
        for (int i = 1; i <= 20; i++) {
            Post post = new Post("User " + i, "This is post " + i);
            postContainer.getChildren().add(post);
        }
    }
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
}
