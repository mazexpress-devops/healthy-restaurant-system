package com.healthyrestaurant.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderTicket {
    private final int id;
    private final int tableNumber;
    private final OrderType orderType;
    private final OrderStatus status;
    private final BigDecimal subtotal;
    private final double calories;
    private final double proteinGrams;
    private final double carbsGrams;
    private final double fatGrams;
    private final String healthNotes;
    private final LocalDateTime createdAt;
    private final List<String> lines;

    public OrderTicket(
            int id,
            int tableNumber,
            OrderType orderType,
            OrderStatus status,
            BigDecimal subtotal,
            double calories,
            double proteinGrams,
            double carbsGrams,
            double fatGrams,
            String healthNotes,
            LocalDateTime createdAt,
            List<String> lines) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.orderType = orderType;
        this.status = status;
        this.subtotal = subtotal;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
        this.healthNotes = healthNotes;
        this.createdAt = createdAt;
        this.lines = new ArrayList<>(lines);
    }

    public int getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public double getCalories() {
        return calories;
    }

    public double getProteinGrams() {
        return proteinGrams;
    }

    public double getCarbsGrams() {
        return carbsGrams;
    }

    public double getFatGrams() {
        return fatGrams;
    }

    public String getHealthNotes() {
        return healthNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
