package com.healthyrestaurant.dao;

import com.healthyrestaurant.config.Database;
import com.healthyrestaurant.model.CustomerProfile;
import com.healthyrestaurant.model.HealthCondition;
import com.healthyrestaurant.model.Ingredient;
import com.healthyrestaurant.model.IngredientCategory;
import com.healthyrestaurant.model.MealSummary;
import com.healthyrestaurant.model.OrderStatus;
import com.healthyrestaurant.model.OrderTicket;
import com.healthyrestaurant.model.OrderType;
import com.healthyrestaurant.model.ReadyMeal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDao {
    public int createReadyMealOrder(int tableNumber, ReadyMeal meal) throws SQLException {
        MealSummary summary = new MealSummary();
        summary.addReadyMeal(meal);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int orderId = insertOrder(
                        connection,
                        null,
                        tableNumber,
                        OrderType.READY_MEAL,
                        summary,
                        "طلب وجبة جاهزة من قائمة الطعام.");
                int itemId = insertOrderItem(connection, orderId, meal.getId(), meal.getName(), summary);
                insertReadyMealIngredients(connection, itemId, meal.getId());
                connection.commit();
                return orderId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public int createCustomOrder(
            CustomerProfile profile,
            List<Ingredient> ingredients,
            MealSummary summary,
            String healthNotes) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int customerId = insertCustomer(connection, profile);
                insertCustomerConditions(connection, customerId, profile.getConditions());

                int orderId = insertOrder(
                        connection,
                        customerId,
                        profile.getTableNumber(),
                        OrderType.CUSTOM_MEAL,
                        summary,
                        healthNotes);
                int itemId = insertOrderItem(connection, orderId, null, "وجبة مخصصة", summary);
                for (Ingredient ingredient : ingredients) {
                    insertOrderItemIngredient(connection, itemId, ingredient);
                }

                connection.commit();
                return orderId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<OrderTicket> findKitchenOrders() throws SQLException {
        String sql = "SELECT * FROM orders WHERE status IN ('NEW', 'PREPARING') ORDER BY created_at ASC LIMIT 50";
        return findTicketsBySql(sql);
    }

    public List<OrderTicket> findRecentOrders(int limit) throws SQLException {
        String sql = "SELECT * FROM orders ORDER BY created_at DESC LIMIT ?";
        List<OrderTicket> tickets = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tickets.add(mapTicket(connection, resultSet));
                }
            }
        }
        return tickets;
    }

    public Optional<OrderTicket> findTicket(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTicket(connection, resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public void updateStatus(int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public void updateOrder(int orderId, int tableNumber, OrderStatus status, java.math.BigDecimal subtotal, String healthNotes)
            throws SQLException {
        String sql = "UPDATE orders SET table_number = ?, status = ?, subtotal = ?, health_notes = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, tableNumber);
            statement.setString(2, status.name());
            statement.setBigDecimal(3, subtotal);
            statement.setString(4, healthNotes);
            statement.setInt(5, orderId);
            statement.executeUpdate();
        }
    }

    public void deleteOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.executeUpdate();
        }
    }

    private List<OrderTicket> findTicketsBySql(String sql) throws SQLException {
        List<OrderTicket> tickets = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tickets.add(mapTicket(connection, resultSet));
            }
        }
        return tickets;
    }

    private int insertCustomer(Connection connection, CustomerProfile profile) throws SQLException {
        String sql = "INSERT INTO customers (table_number, age, weight_kg, height_cm, goal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, profile.getTableNumber());
            statement.setInt(2, profile.getAge());
            statement.setDouble(3, profile.getWeightKg());
            statement.setDouble(4, profile.getHeightCm());
            statement.setString(5, profile.getGoal().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("لم يتم إنشاء رقم للزبون.");
    }

    private void insertCustomerConditions(
            Connection connection,
            int customerId,
            Iterable<HealthCondition> conditions) throws SQLException {
        String sql = "INSERT INTO customer_conditions (customer_id, condition_code) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            boolean hasRows = false;
            for (HealthCondition condition : conditions) {
                statement.setInt(1, customerId);
                statement.setString(2, condition.name());
                statement.addBatch();
                hasRows = true;
            }
            if (hasRows) {
                statement.executeBatch();
            }
        }
    }

    private int insertOrder(
            Connection connection,
            Integer customerId,
            int tableNumber,
            OrderType orderType,
            MealSummary summary,
            String healthNotes) throws SQLException {
        String sql = "INSERT INTO orders "
                + "(customer_id, table_number, order_type, status, subtotal, calories, protein_g, carbs_g, fat_g, health_notes) "
                + "VALUES (?, ?, ?, 'NEW', ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (customerId == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, customerId);
            }
            statement.setInt(2, tableNumber);
            statement.setString(3, orderType.name());
            statement.setBigDecimal(4, summary.getPrice());
            statement.setDouble(5, summary.getCalories());
            statement.setDouble(6, summary.getProteinGrams());
            statement.setDouble(7, summary.getCarbsGrams());
            statement.setDouble(8, summary.getFatGrams());
            statement.setString(9, healthNotes);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("لم يتم إنشاء رقم للطلب.");
    }

    private int insertOrderItem(
            Connection connection,
            int orderId,
            Integer readyMealId,
            String name,
            MealSummary summary) throws SQLException {
        String sql = "INSERT INTO order_items "
                + "(order_id, ready_meal_id, name, quantity, unit_price, calories, protein_g, carbs_g, fat_g) "
                + "VALUES (?, ?, ?, 1, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, orderId);
            if (readyMealId == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, readyMealId);
            }
            statement.setString(3, name);
            statement.setBigDecimal(4, summary.getPrice());
            statement.setDouble(5, summary.getCalories());
            statement.setDouble(6, summary.getProteinGrams());
            statement.setDouble(7, summary.getCarbsGrams());
            statement.setDouble(8, summary.getFatGrams());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("لم يتم إنشاء بند الطلب.");
    }

    private void insertOrderItemIngredient(
            Connection connection,
            int orderItemId,
            Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO order_item_ingredients "
                + "(order_item_id, ingredient_id, ingredient_name, category, unit_price, calories, protein_g, carbs_g, fat_g) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderItemId);
            statement.setInt(2, ingredient.getId());
            statement.setString(3, ingredient.getName());
            statement.setString(4, ingredient.getCategory().name());
            statement.setBigDecimal(5, ingredient.getPrice());
            statement.setDouble(6, ingredient.getCalories());
            statement.setDouble(7, ingredient.getProteinGrams());
            statement.setDouble(8, ingredient.getCarbsGrams());
            statement.setDouble(9, ingredient.getFatGrams());
            statement.executeUpdate();
        }
    }

    private void insertReadyMealIngredients(
            Connection connection,
            int orderItemId,
            int readyMealId) throws SQLException {
        String sql = "INSERT INTO order_item_ingredients "
                + "(order_item_id, ingredient_id, ingredient_name, category, unit_price, calories, protein_g, carbs_g, fat_g) "
                + "SELECT ?, i.id, i.name, i.category, i.price, i.calories, i.protein_g, i.carbs_g, i.fat_g "
                + "FROM ingredients i "
                + "JOIN ready_meal_ingredients rmi ON rmi.ingredient_id = i.id "
                + "WHERE rmi.ready_meal_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderItemId);
            statement.setInt(2, readyMealId);
            statement.executeUpdate();
        }
    }

    private OrderTicket mapTicket(Connection connection, ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp == null ? null : timestamp.toLocalDateTime();

        return new OrderTicket(
                resultSet.getInt("id"),
                resultSet.getInt("table_number"),
                OrderType.valueOf(resultSet.getString("order_type")),
                OrderStatus.valueOf(resultSet.getString("status")),
                resultSet.getBigDecimal("subtotal"),
                resultSet.getDouble("calories"),
                resultSet.getDouble("protein_g"),
                resultSet.getDouble("carbs_g"),
                resultSet.getDouble("fat_g"),
                resultSet.getString("health_notes"),
                createdAt,
                loadTicketLines(connection, resultSet.getInt("id")));
    }

    private List<String> loadTicketLines(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT id, name, quantity, unit_price FROM order_items WHERE order_id = ?";
        List<ItemLine> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new ItemLine(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getInt("quantity"),
                            resultSet.getString("unit_price")));
                }
            }
        }

        List<String> lines = new ArrayList<>();
        for (ItemLine item : items) {
            lines.add("- " + item.name + " x" + item.quantity + " | " + item.unitPrice + " د.ل");
            lines.addAll(loadIngredientLines(connection, item.id));
        }
        return lines;
    }

    private List<String> loadIngredientLines(Connection connection, int orderItemId) throws SQLException {
        String sql = "SELECT ingredient_name, category, unit_price, calories "
                + "FROM order_item_ingredients WHERE order_item_id = ? ORDER BY category, ingredient_name";
        List<String> lines = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderItemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String category = IngredientCategory.valueOf(resultSet.getString("category")).getDisplayName();
                    lines.add("  * " + resultSet.getString("ingredient_name")
                            + " (" + category + ")"
                            + " | " + resultSet.getString("calories") + " سعرة"
                            + " | " + resultSet.getString("unit_price") + " د.ل");
                }
            }
        }
        return lines;
    }

    private static class ItemLine {
        private final int id;
        private final String name;
        private final int quantity;
        private final String unitPrice;

        private ItemLine(int id, String name, int quantity, String unitPrice) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
