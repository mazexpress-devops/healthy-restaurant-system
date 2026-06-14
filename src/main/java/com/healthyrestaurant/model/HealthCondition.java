package com.healthyrestaurant.model;

public enum HealthCondition {
    DIABETES("سكري"),
    LACTOSE_INTOLERANCE("حساسية لاكتوز"),
    GLUTEN_INTOLERANCE("حساسية غلوتين"),
    HYPERTENSION("ضغط"),
    FATTY_LIVER("دهون على الكبد");

    private final String displayName;

    HealthCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
