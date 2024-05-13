package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class signController {

    @FXML
    private Button back;


    public void initialize (){
        ImageView imageView = new ImageView(getClass().getResource("/img/134226-48.png").toExternalForm());
        // Set the size of the ImageView (resize the icon)
        double desiredIconSize = 48; // Change this to your desired size
        imageView.setFitWidth(desiredIconSize);
        imageView.setFitHeight(desiredIconSize);
        back.setGraphic(imageView);
        back.setStyle("-fx-background-color: transparent; ");
    }



    public void goBack(ActionEvent event) throws IOException {

        Parent home_page = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Scene hp_scene = new Scene(home_page);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        app_stage.setScene(hp_scene);
        app_stage.show();
    }

}