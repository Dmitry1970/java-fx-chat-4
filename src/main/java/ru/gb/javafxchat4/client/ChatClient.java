package ru.gb.javafxchat4.client;

import javafx.application.Platform;
import ru.gb.javafxchat4.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static ru.gb.javafxchat4.Command.*;

public class ChatClient {               // взаимодействие пользователя с сервером

    private Socket socket;              // сокет клиента
    private DataInputStream in;
    private DataOutputStream out;
    private final ChatController controller;  // ссылка на контроллер

    public ChatClient(ChatController controller) {
        this.controller = controller;

    }

    public void openConnection() throws IOException {    // открываем соединение в случае успешного соединения
        socket = new Socket("localhost", 8189);                // инициализируем сокет
        in = new DataInputStream(socket.getInputStream());              // чтение сообщений
        out = new DataOutputStream(socket.getOutputStream());           // запись сообщений
        new Thread(() -> {
            try {
                if (waitAuth()) {         // ожидание успешной авторизации
                    readMessages();      // читаем сообщение, после авторизации
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }

        }).start();
    }

    private boolean waitAuth() throws IOException {   // ожидание авторизации
        while (true) {
            final String message = in.readUTF();  // читаем сообщение от сервера
            final Command command = getCommand(message);
            final String[] params = command.parse(message);
            if (command == AUTHOK) { //  /authok  nick1  ожидание от сервера команды "/authok" с nick
                final String nick = params[0];
                controller.setAuth(true);  // в случ. успешн. аутент. скрыв. поле логин и пароля и показ. форму для ввода сообщений
                controller.addMessage("Успешная авторизация под ником " + nick);
                return true;   // в случае успешной аутентификации
            }
            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(params[0]));
                continue;
            }
            if (command == STOP) {
                Platform.runLater(() -> controller.showError("Истекло время на авторизацию, перезапустите приложение"));
                try {
                    Thread.sleep(5000); // Без sleep пользователь не увидит сообщение об ошибке.
                    sendMessage(END);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }

        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
       }
        System.exit(0);
    }

    private void readMessages() throws IOException { // читаем сообщения после успешной аутентификации
        while (true) {
            final String message = in.readUTF();
            final Command command = getCommand(message);
            if (END == command) {                           // клиент, получив END,
                controller.setAuth(false);  // разлогинивается
                break;
            }
            final String[] params = command.parse(message);
            if (ERROR == command) {
                String messageError = params[0];
                Platform.runLater(() -> controller.showError(messageError));
                continue;
            }
            if (MESSAGE == command) {
                Platform.runLater(() -> controller.addMessage(params[0]));  // отображение простого сообщения
            }
            if (CLIENTS == command) {                                       // список клиентов
                Platform.runLater(() -> controller.updateClientsList(params));
            }
        }
    }

    private void sendMessage(String message) {          // отправка сообщения на сервер
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}



