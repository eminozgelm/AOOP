package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class app extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(app.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 650);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        //stage.setMinHeight(650);
        //stage.setMinWidth(1000);
        //stage.setMaxHeight(1080);
        //stage.setMaxWidth(1920);

        stage.show();





    }

    public static void main(String[] args) {
        launch();
    }
}