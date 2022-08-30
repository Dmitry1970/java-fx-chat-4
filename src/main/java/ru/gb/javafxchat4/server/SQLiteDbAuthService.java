package ru.gb.javafxchat4.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteDbAuthService extends SQLiteDatabaseConnector implements AuthService {

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        try {
            PreparedStatement stmt = getConnection()
                    .prepareStatement("select username from auth where login = ? and password = ?");
            stmt.setString(1, login);
            stmt.setString(2, password);

            ResultSet resultSet = stmt.executeQuery();

            return resultSet.getString(1);
        } catch (SQLException e) {
            System.out.println("Неудалось получить username: " + e.getMessage());
            return null;
        }
    }
}
