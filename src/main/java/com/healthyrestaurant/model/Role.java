package com.healthyrestaurant.model;

public enum Role {
    ADMIN("مدير النظام"),
    CHEF("الشيف");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
