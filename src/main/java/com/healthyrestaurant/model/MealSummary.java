package com.healthyrestaurant.model;

import java.math.BigDecimal;

public class MealSummary {
    private BigDecimal price = BigDecimal.ZERO;
    private double calories;
    private double proteinGrams;
    private double carbsGrams;
    private double fatGrams;

    public void addIngredient(Ingredient ingredient) {
        price = price.add(ingredient.getPrice());
        calories += ingredient.getCalories();
        proteinGrams += ingredient.getProteinGrams();
        carbsGrams += ingredient.getCarbsGrams();
        fatGrams += ingredient.getFatGrams();
    }

    public void addReadyMeal(ReadyMeal meal) {
        price = price.add(meal.getPrice());
        calories += meal.getCalories();
        proteinGrams += meal.getProteinGrams();
        carbsGrams += meal.getCarbsGrams();
        fatGrams += meal.getFatGrams();
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
}
