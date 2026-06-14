package com.healthyrestaurant.model;

public enum Goal {
    MAINTAIN("تثبيت الوزن", 0),
    CLEAN_BULK("زيادة نظيفة", 250),
    BULK("زيادة عشوائية", 450),
    CUT("تنشيف", -400);

    private final String displayName;
    private final int calorieAdjustment;

    Goal(String displayName, int calorieAdjustment) {
        this.displayName = displayName;
        this.calorieAdjustment = calorieAdjustment;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCalorieAdjustment() {
        return calorieAdjustment;
    }
}
