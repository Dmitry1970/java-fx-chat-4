package ru.gb.javafxchat4.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {  // имплементируем интерфейс AuthService

    private static class UserData {         // данные пользователя
        private String nick;
        private String login;
        private String password;

        public UserData(String nick, String login, String password) {
            this.nick = nick;
            this.login = login;
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    private List<UserData> users;   // список клиентов

    public InMemoryAuthService() {
        users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {       // добавляем 5 пользователей
            users.add(new UserData("nick" + i, "login" + i, "pass" + i));
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData user : users) {   // просматриваем пользователей
            if (login.equals(user.getLogin()) && password.equals(user.getPassword())) {  // до тех пор, пока встретятся пользователи с таким логином и паролем
                return user.getNick();  // как встретился - возвращаем nick пользователя
            }
        }
        return null;   // если такой позователь не встретился - выходим из цикла и возвращаем null
    }

    @Override
    public void close() throws IOException {
        System.out.println("Сервис аутентификации остановлен");
    }
}
