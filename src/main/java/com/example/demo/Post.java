package com.example.demo;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class Post extends Pane {

    private VBox content;

    public Post(String owner, String contentText) {
        // Create a VBox to hold post content
        content = new VBox();
        content.setSpacing(5); // Set spacing between elements

        // Create labels for owner and content text
        Label ownerLabel = new Label(owner);
        Label contentLabel = new Label(contentText);

        // Add labels to the VBox
        content.getChildren().addAll(ownerLabel, contentLabel);

        // Set size and style of the post
        setMinWidth(300); // Set minimum width of the post
        setMaxWidth(400); // Set maximum width of the post
        setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 10px;");

        // Add the VBox to the Pane (Post)
        getChildren().add(content);
    }
}
