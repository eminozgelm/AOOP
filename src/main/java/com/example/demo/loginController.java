package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class loginController {

    private static final String DB_URL = "jdbc:sqlite:your_database_name.db";

    @FXML
    private Text errorText;
    @FXML
    private Button button;

    @FXML
    private PasswordField passField;

    @FXML
    private TextField userField;
    public void tryLogin(ActionEvent event) throws IOException {
        String userName = userField.getText();
        String passWord = passField.getText();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Attempt to authenticate the user
            int user_id= Dbase.authenticateUser(conn, userName, passWord);
            UserSession us = UserSession.getInstance();
            us.setUserId(user_id);
            wallController wc = new wallController(us);
            // Display appropriate message based on authentication result
            if (user_id > 0) {
                Parent home_page = FXMLLoader.load(getClass().getResource("mainPage.fxml"));
                Scene hp_scene = new Scene(home_page);
                Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                app_stage.setScene(hp_scene);
                app_stage.show();
            } else {
                errorText.setText("Invalid username or password.");
                errorText.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            errorText.setText("Database connection error.");
            errorText.setStyle("-fx-text-fill: red;");
            System.err.println("Error: " + e.getMessage());
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