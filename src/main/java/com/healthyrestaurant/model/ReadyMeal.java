package com.healthyrestaurant.model;

import java.math.BigDecimal;

public class ReadyMeal {
    private final int id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final double calories;
    private final double proteinGrams;
    private final double carbsGrams;
    private final double fatGrams;
    private final boolean available;

    public ReadyMeal(
            int id,
            String name,
            String description,
            BigDecimal price,
            double calories,
            double proteinGrams,
            double carbsGrams,
            double fatGrams,
            boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
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

    public boolean isAvailable() {
        return available;
    }
}
