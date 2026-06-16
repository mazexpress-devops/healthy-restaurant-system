package com.healthyrestaurant.model;

public class StaffAccount {
    private final int id;
    private final String username;
    private final String fullName;
    private final Role role;
    private final boolean active;

    public StaffAccount(int id, String username, String fullName, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
