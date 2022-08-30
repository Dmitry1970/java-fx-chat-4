package ru.gb.javafxchat4.server;

import java.io.Closeable;

public interface AuthService extends Closeable {   // сервис аутентификации: хранит логины и пароли и возвращает их пользователю
    String getNickByLoginAndPassword(String login, String password);

}
