package com.healthyrestaurant.model;

import java.math.BigDecimal;

public class Ingredient {
    private final int id;
    private final String name;
    private final IngredientCategory category;
    private final String servingLabel;
    private final BigDecimal price;
    private final double calories;
    private final double proteinGrams;
    private final double carbsGrams;
    private final double fatGrams;
    private final boolean containsLactose;
    private final boolean containsGluten;
    private final boolean highSugar;
    private final boolean highSodium;
    private final boolean highFat;
    private final boolean available;

    public Ingredient(
            int id,
            String name,
            IngredientCategory category,
            String servingLabel,
            BigDecimal price,
            double calories,
            double proteinGrams,
            double carbsGrams,
            double fatGrams,
            boolean containsLactose,
            boolean containsGluten,
            boolean highSugar,
            boolean highSodium,
            boolean highFat,
            boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.servingLabel = servingLabel;
        this.price = price;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
        this.containsLactose = containsLactose;
        this.containsGluten = containsGluten;
        this.highSugar = highSugar;
        this.highSodium = highSodium;
        this.highFat = highFat;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IngredientCategory getCategory() {
        return category;
    }

    public String getServingLabel() {
        return servingLabel;
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

    public boolean isContainsLactose() {
        return containsLactose;
    }

    public boolean isContainsGluten() {
        return containsGluten;
    }

    public boolean isHighSugar() {
        return highSugar;
    }

    public boolean isHighSodium() {
        return highSodium;
    }

    public boolean isHighFat() {
        return highFat;
    }

    public boolean isAvailable() {
        return available;
    }
}
