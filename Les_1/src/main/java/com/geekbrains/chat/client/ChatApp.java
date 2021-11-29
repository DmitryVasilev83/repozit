package com.geekbrains.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent parent = FXMLLoader.load(getClass().getResource("src/main/resources/com.geekbrains.chat/client/chat.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.show();
    }
}