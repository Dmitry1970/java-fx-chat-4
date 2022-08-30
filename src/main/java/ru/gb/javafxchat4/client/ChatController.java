package ru.gb.javafxchat4.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ru.gb.javafxchat4.Command;

import java.io.IOException;
import java.util.Optional;

public class ChatController {
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextField loginField;
    @FXML
    private HBox authBox;
    @FXML
    private PasswordField passField;
    @FXML
    private HBox messageBox;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;

    private final ChatClient client;

    private String selectedNick;

    public ChatController() {           // запуск контроллера
        this.client = new ChatClient(this); // инстанс класса, кот. отвечет за коммуникацию с сервером
        while (true) {
            try {
                client.openConnection();  // вызываем метод из ChatClient
                break;                    // если успешное соединение
            } catch (IOException e) {
                showNotification();         // если неуспешное соединение, то сообщение пользователю
            }
        }
    }

    public void showNotification() {
        final Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подключиться к серверу.\n" +
                        "Проверьте, что сервер запущен и доступен",
                new ButtonType("Попробовать снова", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        alert.setTitle("Ошибка подключения");
        final Optional<ButtonType> answer = alert.showAndWait();
        final Boolean isExit = answer                                      // если пользователь хочет выйти
                .map(select -> select.getButtonData().isCancelButton())    // кнопка, нажатая польз. содержит select
                .orElse(false);
        if (isExit) {
            System.exit(0);
        }
    }

    public void clickSendButton() {
        final String message = messageField.getText();
        if (message.isBlank()) {
            return;
        }
        if (selectedNick != null) {
            client.sendMessage(Command.PRIVATE_MESSAGE, selectedNick, message);
            selectedNick = null;
        } else {
            client.sendMessage(Command.MESSAGE, message);
        }
        messageField.clear();
        messageField.requestFocus();
    }

    public void addMessage(String message) {
        messageArea.appendText(message + "\n");  // передаём сообщение в поле сообщений
    }

    public void setAuth(boolean success) {  // в случае успешной аутентификации
        authBox.setVisible(!success);  // скрываем поля аутентификации(логин и пароль)
        messageBox.setVisible(success);  // показываем поле для сообщений
    }

    public void signinBtnClick() {   // пользователь нажимает кнопку "Sign in"
        client.sendMessage(Command.AUTH, loginField.getText(), passField.getText());  // отправка сообщения на сервер на аутентификацию
    }

    public void showError(String errorMessage) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage,
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Error!");
        alert.showAndWait();
    }

    public void selectClient(MouseEvent mouseEvent) {            // вызываем клиента нажатием мыши
        if (mouseEvent.getClickCount() == 2) {                   //   2 клика
            final String selectedNick = clientList.getSelectionModel().getSelectedItem();
            if (selectedNick != null && !selectedNick.isEmpty()) {
                this.selectedNick = selectedNick;
            }
        }
    }

    public void updateClientsList(String[] clients) {
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);

    }

    public void signOutClick() {
        client.sendMessage(Command.END);
    }

    public ChatClient getClient() {
        return client;
    }
}