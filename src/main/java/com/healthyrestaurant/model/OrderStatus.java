package com.healthyrestaurant.model;

public enum OrderStatus {
    NEW("جديد"),
    PREPARING("قيد التحضير"),
    READY("جاهز"),
    COMPLETED("مكتمل"),
    CANCELLED("ملغي");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
