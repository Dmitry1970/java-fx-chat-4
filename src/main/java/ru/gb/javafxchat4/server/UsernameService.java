package ru.gb.javafxchat4.server;

import java.io.Closeable;

public interface UsernameService extends Closeable {

    void updateUsername(String login, String newUsername);
}
