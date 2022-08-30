package ru.gb.javafxchat4.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.gb.javafxchat4.Command;

import java.io.IOException;

public class ChatClientApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {    // запускаем код клиента
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApp.class.getResource("client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("GB Chat client");
        stage.setScene(scene);
        stage.show();

        ChatController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(event -> controller.getClient().sendMessage(Command.END));  // при закрытии формы
        // отправляется команда END, но сервер продолжает работать
    }

    public static void main(String[] args) {
        launch();
    }
}