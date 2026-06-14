package com.healthyrestaurant.service;

import com.healthyrestaurant.model.CustomerProfile;
import com.healthyrestaurant.model.HealthCondition;
import com.healthyrestaurant.model.Ingredient;
import com.healthyrestaurant.model.MealSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class NutritionService {
    public double estimateDailyCalories(CustomerProfile profile) {
        double bmr = (10 * profile.getWeightKg()) + (6.25 * profile.getHeightCm()) - (5 * profile.getAge()) + 5;
        double maintenance = bmr * 1.35;
        return Math.max(1200, maintenance + profile.getGoal().getCalorieAdjustment());
    }

    public double estimateMealTarget(CustomerProfile profile) {
        return estimateDailyCalories(profile) / 3.0;
    }

    public List<String> validateIngredient(Ingredient ingredient, CustomerProfile profile) {
        List<String> errors = new ArrayList<>();

        if (profile.hasCondition(HealthCondition.LACTOSE_INTOLERANCE) && ingredient.isContainsLactose()) {
            errors.add("المكون يحتوي على لاكتوز، لذلك لا يناسب حساسية اللاكتوز.");
        }
        if (profile.hasCondition(HealthCondition.GLUTEN_INTOLERANCE) && ingredient.isContainsGluten()) {
            errors.add("المكون يحتوي على غلوتين، لذلك لا يناسب حساسية الغلوتين.");
        }
        if (profile.hasCondition(HealthCondition.DIABETES) && ingredient.isHighSugar()) {
            errors.add("المكون عالي السكر، لذلك تم منعه لحالة السكري.");
        }
        if (profile.hasCondition(HealthCondition.HYPERTENSION) && ingredient.isHighSodium()) {
            errors.add("المكون عالي الصوديوم، لذلك لا يناسب حالة الضغط.");
        }
        if (profile.hasCondition(HealthCondition.FATTY_LIVER) && ingredient.isHighFat()) {
            errors.add("المكون عالي الدهون، لذلك لا يناسب حالة دهون الكبد.");
        }

        return errors;
    }

    public List<String> buildSmartFeedback(CustomerProfile profile, MealSummary summary) {
        List<String> feedback = new ArrayList<>();
        double target = estimateMealTarget(profile);

        if (summary.getCalories() > target * 1.15) {
            feedback.add("السعرات أعلى من هدف الوجبة المقترح بحوالي "
                    + format(summary.getCalories() - target) + " سعرة.");
        } else if (summary.getCalories() < target * 0.75) {
            feedback.add("السعرات أقل من هدف الوجبة المقترح، يمكن إضافة مصدر نشويات أو بروتين مناسب.");
        } else {
            feedback.add("السعرات مناسبة تقريبا لهدفك الحالي.");
        }

        if (summary.getProteinGrams() < 20) {
            feedback.add("البروتين منخفض نسبيا؛ إضافة مصدر بروتين صحي قد تجعل الوجبة أفضل.");
        }
        if (profile.hasCondition(HealthCondition.FATTY_LIVER) && summary.getFatGrams() > 20) {
            feedback.add("كمية الدهون مرتفعة لحالة دهون الكبد.");
        }
        if (profile.hasCondition(HealthCondition.DIABETES) && summary.getCarbsGrams() > 55) {
            feedback.add("الكربوهيدرات مرتفعة نسبيا لحالة السكري.");
        }
        if (feedback.isEmpty()) {
            feedback.add("هذه الوجبة مناسبة مبدئيا للبيانات المدخلة.");
        }

        return feedback;
    }

    public String buildHealthNotes(
            CustomerProfile profile,
            MealSummary summary,
            List<String> feedback,
            List<Ingredient> ingredients) {
        String conditions = profile.getConditions().isEmpty()
                ? "لا توجد حالات صحية مسجلة"
                : profile.getConditions().stream()
                .map(HealthCondition::getDisplayName)
                .collect(Collectors.joining("، "));
        String ingredientNames = ingredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.joining("، "));

        return "الهدف: " + profile.getGoal().getDisplayName()
                + "\nالحالات الصحية: " + conditions
                + "\nالمكونات المختارة: " + ingredientNames
                + "\nالسعرات: " + format(summary.getCalories())
                + " | بروتين: " + format(summary.getProteinGrams())
                + "g | كربوهيدرات: " + format(summary.getCarbsGrams())
                + "g | دهون: " + format(summary.getFatGrams()) + "g"
                + "\nالتنبيهات: " + String.join(" | ", feedback);
    }

    public String format(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
