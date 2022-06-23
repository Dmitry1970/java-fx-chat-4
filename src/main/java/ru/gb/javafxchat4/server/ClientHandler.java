package ru.gb.javafxchat4.server;

import ru.gb.javafxchat4.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Socket socket;
    private ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;


    private String nick;
    private AuthService authService;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {

        try {
            this.server = server;
            this.socket = socket;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    authenticate();
                    readMessages();
                } finally {
                    System.out.println("Клиент " + nick + " отключился");
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessages() {
        while (true) {
            try {
                final String message = in.readUTF();
                final Command command = Command.getCommand(message);
                if (command == Command.END) {
                    break;
                }
                if (command == Command.PRIVATE_MESSAGE) {
                    final String[] params = command.parse(message);
                    server.sendPrivateMessage(this, params[0], params[1]);
                    continue;
                }
                server.broadcast(Command.MESSAGE, nick + ": " + command.parse(message)[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void authenticate() {   //  /auth login1 pass1
        while (true) {
            try {
                final String message = in.readUTF();
                if (Command.isCommand(message)) {
                    final Command command = Command.getCommand(message);
                    if (command == Command.AUTH) {
                        final String[] params = command.parse(message);    // получаем параметры сообщения
                        final String login = params[0];
                        final String password = params[1];
                        final String nick = authService.getNickByLoginAndPassword(login, password);
                        if (nick != null) {
                            if (server.isNickBusy(nick)) {
                                sendMessage(Command.ERROR, "Пользователь уже авторизован");
                                continue;
                            }
                            sendMessage(Command.AUTHOK, nick);
                            this.nick = nick;
                            server.broadcast(Command.MESSAGE, "Пользователь " + nick + " зашёл в чат");
                            server.subscribe(this);
                            break;
                        } else {
                            sendMessage(Command.ERROR, "Неверные логин и пароль");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }


    public void closeConnection() {

        sendMessage(Command.END);
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
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getNick() {
        return nick;
    }


}

