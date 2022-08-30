package ru.gb.javafxchat4.server;

import ru.gb.javafxchat4.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {   // общение с клиентом происходит внутри этого класса

    private static final int AUTH_TIMEOUT = 120_000;

    private Socket socket;          // отвечает за соединение с клиентом(для каждого клиента свой сокет)
    private ChatServer server;      // ссылка на сервер, знает всё о клиентах
    private DataInputStream in;
    private DataOutputStream out;
    private String login;
    private String nick;
    private AuthService authService;  // поле сервиса аутентификации
    private UsernameService usernameService;
    private Thread timeoutThread;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService, UsernameService usernameService) {

        try {
            this.server = server;                                            // инициализируем сервер
            this.socket = socket;                                            // инициализируем сокет клиента(у каждого свой)
            this.authService = authService;
            this.usernameService = usernameService;
            this.in = new DataInputStream(socket.getInputStream());           // чтение сообщений
            this.out = new DataOutputStream(socket.getOutputStream());        // запись сообщений

            this.timeoutThread = new Thread(() -> {
                try {
                    Thread.sleep(AUTH_TIMEOUT);
                    sendMessage(Command.STOP); // Если в другом потоке не будет вызван метод interrupt, то мы попадём
                    // сюда
                } catch (InterruptedException e) {      // В другом потоке была успешная авторизация
                    System.out.println("Успешная авторизация");
                }
            });
            timeoutThread.start();

            new Thread(() -> {        // у каждого клиента свой поток
                try {
                    if (authenticate()) {   // перед чтением сообщения необх. пользователя аутентифицировать по логину и паролю
                        readMessages();
                    }// читаем сообщение от клиента в отдельном потоке, чтобы не блокировать осн. поток
                } finally {
                    System.out.println("Клиент " + nick + " отключился");
                    closeConnection();  // закрываем соединение с клиентом
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean authenticate() {   //  /auth login1 pass1 - метод аутентификации
    while (true) {
        try {
            final String message = in.readUTF();  //читаем сообщение от клиента
            final Command command = Command.getCommand(message);
            if (command == Command.END) {
                return false;
            }
            if (command == Command.AUTH) {
                    final String[] params = command.parse(message);    // делим сообщ. и получаем параметры сообщ. по пробелам
                    final String login = params[0];
                    final String password = params[1];
                    final String nick = authService.getNickByLoginAndPassword(login, password);  // проверяем логин и пароль
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {   // проверка, что такой пользователь ещё не зарегистрирован
                            sendMessage(Command.ERROR, "Пользователь уже авторизован");
                            continue;
                        }
                        this.timeoutThread.interrupt(); // при вызове этого метода у спящего треда будет брошено
                        // InterruptedException
                        sendMessage(Command.AUTHOK, nick); // собщение клиенту(nick), что авторизация прошла успешно
                        this.nick = nick;
                        server.broadcast(Command.MESSAGE, "Пользователь " + nick + " зашёл в чат");
                        server.subscribe(this); // добавляем подключившегося пользователя в список клиентов
                        return true;
                    } else {
                        sendMessage(Command.ERROR, "Неверные логин и пароль");
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
        sendMessage(Command.END); // сообщение клиенту перед закрытием соединения
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

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);       // передаём сообщение клиенту
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessages() {    // сервер читает сообщение от клиента
        while (true) {
            try {
                final String message = in.readUTF();  // блокирующий вызов - пока клиент что-либо напишет
                final Command command = Command.getCommand(message);
                if (command == Command.END) {    // крутимся в цикле, пока клиент пошлёт "/end"
                    break;
                }
                if (command == Command.PRIVATE_MESSAGE) {
                    final String[] params = command.parse(message);
                    server.sendPrivateMessage(this, params[0], params[1]);// this - от кого, [0] - кому, [1]
                    continue;
                }
                if (command == Command.CHANGE_USERNAME) {
                    String newUsername = command.parse(message)[0];
                    rename(newUsername);
                    continue;
                }
                server.broadcast(Command.MESSAGE, nick + ": " + command.parse(message)[0]); // клиент прислал сообщение серверу
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void rename(String newUsername) {
        usernameService.updateUsername(login, newUsername);
        setNick(newUsername);
        server.broadcastClientsList();
    }

    public String getNick() {
        return nick;
    }
    public void setNick(String nick) {
        this.nick = nick;
    }

}

