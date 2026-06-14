package com.healthyrestaurant.service;

import com.healthyrestaurant.dao.OrderDao;
import com.healthyrestaurant.model.CustomerProfile;
import com.healthyrestaurant.model.Ingredient;
import com.healthyrestaurant.model.MealSummary;
import com.healthyrestaurant.model.ReadyMeal;

import java.sql.SQLException;
import java.util.List;

public class OrderService {
    private final OrderDao orderDao;
    private final NutritionService nutritionService;

    public OrderService(OrderDao orderDao, NutritionService nutritionService) {
        this.orderDao = orderDao;
        this.nutritionService = nutritionService;
    }

    public int placeReadyMealOrder(int tableNumber, ReadyMeal meal) throws SQLException {
        return orderDao.createReadyMealOrder(tableNumber, meal);
    }

    public int placeCustomMealOrder(
            CustomerProfile profile,
            List<Ingredient> ingredients,
            MealSummary summary) throws SQLException {
        List<String> feedback = nutritionService.buildSmartFeedback(profile, summary);
        String notes = nutritionService.buildHealthNotes(profile, summary, feedback, ingredients);
        return orderDao.createCustomOrder(profile, ingredients, summary, notes);
    }
}
