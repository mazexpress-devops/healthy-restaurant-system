package com.healthyrestaurant.dao;

import com.healthyrestaurant.config.Database;
import com.healthyrestaurant.model.DiningTableAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiningTableDao {
    public List<DiningTableAccount> findOpenAccounts() throws SQLException {
        String sql = "SELECT id, table_number, account_name, status, active "
                + "FROM dining_tables WHERE active = TRUE AND status = 'OPEN' ORDER BY table_number";
        List<DiningTableAccount> tables = new ArrayList<DiningTableAccount>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tables.add(mapTable(resultSet));
            }
        }
        return tables;
    }

    public List<DiningTableAccount> findAll() throws SQLException {
        String sql = "SELECT id, table_number, account_name, status, active "
                + "FROM dining_tables ORDER BY table_number";
        List<DiningTableAccount> tables = new ArrayList<DiningTableAccount>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tables.add(mapTable(resultSet));
            }
        }
        return tables;
    }

    public Optional<DiningTableAccount> findOpenByTableNumber(int tableNumber) throws SQLException {
        String sql = "SELECT id, table_number, account_name, status, active "
                + "FROM dining_tables WHERE table_number = ? AND active = TRUE AND status = 'OPEN'";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, tableNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTable(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    private DiningTableAccount mapTable(ResultSet resultSet) throws SQLException {
        return new DiningTableAccount(
                resultSet.getInt("id"),
                resultSet.getInt("table_number"),
                resultSet.getString("account_name"),
                resultSet.getString("status"),
                resultSet.getBoolean("active"));
    }
}
