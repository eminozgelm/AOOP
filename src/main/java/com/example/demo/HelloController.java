package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }


    public int tryLogin(String username,String pasw) {
        if (username == "ajenk" && pasw == "123"){
            return 1;
        }
        else {
            return 0;
        }
    }
    }