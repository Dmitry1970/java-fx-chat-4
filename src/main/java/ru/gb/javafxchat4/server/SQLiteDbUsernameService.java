package ru.gb.javafxchat4.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteDbUsernameService extends SQLiteDatabaseConnector implements UsernameService {
    @Override
    public void updateUsername(String login, String newUsername) {
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement("""
                    update auth
                    set username = ?
                    where login = ?
                    """);

            stmt.setString(1, newUsername);
            stmt.setString(2, login);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Не удалось переименовать " + login + ": " + e.getMessage());
        }
    }
}
