package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.Node;

import java.io.IOException;

public class loginController {

    private final String userName = "ajenkeÇakan";
    private final int pass = 31;
    @FXML
    private Button button;

    @FXML
    private PasswordField passField;

    @FXML
    private TextField userField;
    public void tryLogin(ActionEvent event) throws IOException {
        if (userField.getText().equals(userName) && Integer.parseInt(passField.getText()) == pass) {
            Parent home_page = FXMLLoader.load(getClass().getResource("mainPage.fxml"));
            Scene hp_scene = new Scene(home_page);
            Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            app_stage.setScene(hp_scene);
            app_stage.show();
        }


    }

    public void goToSign(ActionEvent event) throws IOException {
        Parent home_page = FXMLLoader.load(getClass().getResource("signPage.fxml"));
        Scene hp_scene = new Scene(home_page);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        app_stage.setScene(hp_scene);
        app_stage.show();
    }

}