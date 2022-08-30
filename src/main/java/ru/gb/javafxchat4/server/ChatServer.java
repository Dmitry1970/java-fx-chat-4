package ru.gb.javafxchat4.server;

import ru.gb.javafxchat4.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatServer {               // взаимодействие с сервером

    private final Map<String, ClientHandler> clients;  // список клиентов, кот. подкл. к серверу, после аутентификации

    public ChatServer() {
        this.clients = new HashMap<>();    // инициализация в конструкторе списка клиентов
    }

    public void run() {         // запуск сервера
        try (ServerSocket serverSocket = new ServerSocket(8189); // создаём серверный сокет
             AuthService authService = new SQLiteDbAuthService();   // инициализируем сервис аутентификации
            UsernameService usernameService = new SQLiteDbUsernameService()) {
            while (true) {
                System.out.println("Ожидаю подключения...");
                final Socket socket = serverSocket.accept();        // получаем клиентский сокет(ожидаем подключение клиента)
                new ClientHandler(socket, this, authService, usernameService); // передаем socket, класс ChatServer,
                // аутентиф. в конструктор ClientHandler
                System.out.println("Клиент подключен");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void subscribe(ClientHandler client) {  // добавляем нового клиента
        clients.put(client.getNick(), client);  // добавляем клиента, кот. аутентиф. в список клиентов
        broadcastClientsList();     // обновляем список клиентов
    }

    public boolean isNickBusy(String nick) {   // проверка занят ник или нет
        return clients.get(nick) != null;      // ник занят

    }

    public void broadcastClientsList() {
        final String nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.joining(" "));  // объединяем
        broadcast(Command.CLIENTS, nicks);   // список ников
    }

    public void broadcast(Command command, String message) {  // рассылка сообщений всем клиентам
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command, message);  // для каждого клиента
        }
    }

    public void unsubscribe(ClientHandler client) {   // если клиент вышел,
        clients.remove(client.getNick());       // убираем его из списка клиентов
        broadcastClientsList();                 // обновляем список клиентов
    }

    public void sendPrivateMessage(ClientHandler from, String nickTo, String message) {
        final ClientHandler clientTo = clients.get(nickTo);  // кому шлём сообщение
        if (clientTo == null) {
            from.sendMessage(Command.ERROR, "Пользователь не авторизован!");  // сообщение отправителю
            return;
        }
        clientTo.sendMessage(Command.MESSAGE, "От " + from.getNick() + ": " + message);
        from.sendMessage(Command.MESSAGE, "Участнику " + nickTo + ": " + message);
    }
}
