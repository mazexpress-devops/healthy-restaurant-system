package com.healthyrestaurant.dao;

import com.healthyrestaurant.config.Database;
import com.healthyrestaurant.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {
    public Optional<User> findActiveByCredentials(String username, String password) throws SQLException {
        String sql = "SELECT id, username, full_name, role "
                + "FROM users WHERE username = ? AND password_hash = ? AND active = TRUE";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new User(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getString("full_name"),
                            resultSet.getInt("role")));
                }
            }
        }
        return Optional.empty();
    }
}
