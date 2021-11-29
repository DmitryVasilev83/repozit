package com.geekbrains.chat.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatController implements Initializable {

    public TextField input;
    public ListView<String> listView; // список файлов в директории клиента
    private IoNet net;

    public void sendMsg(ActionEvent actionEvent) throws IOException {
        net.sendMsg(input.getText());
        input.clear();

    }

    public void sendFile(ActionEvent actionEvent) throws IOException{    // отправить выбранный в listView файл на сервер

        String filePath = listView.getSelectionModel().getSelectedItem();

        net.sendMsg("filePath/" + filePath);    // отправляем на сервер имя файла

        File file = new File(filePath);             // отправлемя файл на сервер
        byte[] buf = new byte[8192];

        try (FileInputStream is = new FileInputStream(file)) {
            int read;
            try {while ((read = is.read(buf)) != -1) {
                    net.sendByteMsg(buf);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void addMessage(String msg) {
        Platform.runLater(() -> listView.getItems().add(msg));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            net = new IoNet(this::addMessage, socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
