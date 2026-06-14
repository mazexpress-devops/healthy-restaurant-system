package com.healthyrestaurant.dao;

import com.healthyrestaurant.config.Database;
import com.healthyrestaurant.model.ReadyMeal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReadyMealDao {
    public List<ReadyMeal> findAllAvailable() throws SQLException {
        String sql = "SELECT * FROM ready_meals WHERE available = TRUE ORDER BY name";
        List<ReadyMeal> meals = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                meals.add(mapMeal(resultSet));
            }
        }
        return meals;
    }

    public Optional<ReadyMeal> findById(int id) throws SQLException {
        String sql = "SELECT * FROM ready_meals WHERE id = ? AND available = TRUE";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapMeal(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    private ReadyMeal mapMeal(ResultSet resultSet) throws SQLException {
        return new ReadyMeal(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("price"),
                resultSet.getDouble("calories"),
                resultSet.getDouble("protein_g"),
                resultSet.getDouble("carbs_g"),
                resultSet.getDouble("fat_g"),
                resultSet.getBoolean("available"));
    }
}
