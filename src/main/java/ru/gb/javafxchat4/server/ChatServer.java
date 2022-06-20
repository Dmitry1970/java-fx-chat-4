package ru.gb.javafxchat4.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {               // взаимодействие с сервером

    private final List<ClientHandler> clients;


    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new InMemoryAuthService()) {
            while (true) {
                System.out.println("Ожидаю подключения...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключен");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void sendPrivateMessage(ClientHandler senderName, String receiverName, String message) {
        for (ClientHandler c : clients) {
            if (c.getNick().equalsIgnoreCase(receiverName)) {
                c.sendMessage("от " + senderName.getNick() + ": " + message);
                senderName.sendMessage("пользователю " + receiverName + ": " + message);
                return;
            }
        }
        senderName.sendMessage("Пользователь " + receiverName + " не в сети");
    }
}
