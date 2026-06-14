package com.healthyrestaurant.model;

public enum IngredientCategory {
    PROTEIN("بروتين"),
    CARB("نشويات"),
    FAT("دهون صحية"),
    ADDON("إضافات"),
    SAUCE("صلصات");

    private final String displayName;

    IngredientCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
