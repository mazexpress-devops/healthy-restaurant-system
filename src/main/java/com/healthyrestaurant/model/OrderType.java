package com.healthyrestaurant.model;

public enum OrderType {
    READY_MEAL("وجبة جاهزة"),
    CUSTOM_MEAL("وجبة مخصصة");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
