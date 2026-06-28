package com.healthyrestaurant.dao;

import com.healthyrestaurant.config.Database;
import com.healthyrestaurant.model.Ingredient;
import com.healthyrestaurant.model.IngredientCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IngredientDao {
    public List<Ingredient> findAllAvailable() throws SQLException {
        return findBySql("SELECT * FROM ingredients WHERE available = TRUE ORDER BY category, name");
    }
 

    public Optional<Ingredient> findById(int id) throws SQLException {
        String sql = "SELECT * FROM ingredients WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapIngredient(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredients "
                + "(name, category, serving_label, price, calories, protein_g, carbs_g, fat_g, "
                + "contains_lactose, contains_gluten, high_sugar, high_sodium, high_fat, available) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, ingredient.getName());
            statement.setString(2, ingredient.getCategory().name());
            statement.setString(3, ingredient.getServingLabel());
            statement.setBigDecimal(4, ingredient.getPrice());
            statement.setDouble(5, ingredient.getCalories());
            statement.setDouble(6, ingredient.getProteinGrams());
            statement.setDouble(7, ingredient.getCarbsGrams());
            statement.setDouble(8, ingredient.getFatGrams());
            statement.setBoolean(9, ingredient.isContainsLactose());
            statement.setBoolean(10, ingredient.isContainsGluten());
            statement.setBoolean(11, ingredient.isHighSugar());
            statement.setBoolean(12, ingredient.isHighSodium());
            statement.setBoolean(13, ingredient.isHighFat());
            statement.setBoolean(14, ingredient.isAvailable());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("لم يتم إنشاء رقم للمكون الجديد.");
    }

    public void updateAvailability(int id, boolean available) throws SQLException {
        String sql = "UPDATE ingredients SET available = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, available);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void update(Ingredient ingredient) throws SQLException {
        String sql = "UPDATE ingredients SET "
                + "name = ?, category = ?, serving_label = ?, price = ?, calories = ?, "
                + "protein_g = ?, carbs_g = ?, fat_g = ?, contains_lactose = ?, "
                + "contains_gluten = ?, high_sugar = ?, high_sodium = ?, high_fat = ?, available = ? "
                + "WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ingredient.getName());
            statement.setString(2, ingredient.getCategory().name());
            statement.setString(3, ingredient.getServingLabel());
            statement.setBigDecimal(4, ingredient.getPrice());
            statement.setDouble(5, ingredient.getCalories());
            statement.setDouble(6, ingredient.getProteinGrams());
            statement.setDouble(7, ingredient.getCarbsGrams());
            statement.setDouble(8, ingredient.getFatGrams());
            statement.setBoolean(9, ingredient.isContainsLactose());
            statement.setBoolean(10, ingredient.isContainsGluten());
            statement.setBoolean(11, ingredient.isHighSugar());
            statement.setBoolean(12, ingredient.isHighSodium());
            statement.setBoolean(13, ingredient.isHighFat());
            statement.setBoolean(14, ingredient.isAvailable());
            statement.setInt(15, ingredient.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM ready_meal_ingredients WHERE ingredient_id = ?")) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM ingredients WHERE id = ?")) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private List<Ingredient> findBySql(String sql) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ingredients.add(mapIngredient(resultSet));
            }
        }
        return ingredients;
    }

    private Ingredient mapIngredient(ResultSet resultSet) throws SQLException {
        return new Ingredient(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                IngredientCategory.valueOf(resultSet.getString("category")),
                resultSet.getString("serving_label"),
                resultSet.getBigDecimal("price"),
                resultSet.getDouble("calories"),
                resultSet.getDouble("protein_g"),
                resultSet.getDouble("carbs_g"),
                resultSet.getDouble("fat_g"),
                resultSet.getBoolean("contains_lactose"),
                resultSet.getBoolean("contains_gluten"),
                resultSet.getBoolean("high_sugar"),
                resultSet.getBoolean("high_sodium"),
                resultSet.getBoolean("high_fat"),
                resultSet.getBoolean("available"));
    }
}
