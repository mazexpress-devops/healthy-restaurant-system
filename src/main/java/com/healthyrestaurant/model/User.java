package com.healthyrestaurant.model;

public class User {
    public static final int ROLE_CUSTOMER = 1;
    public static final int ROLE_CHEF = 2;
    public static final int ROLE_ADMIN = 3;

    private final int id;
    private final String username;
    private final String fullName;
    private final int role;

    public User(int id, String username, String fullName, int role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
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

    public int getRole() {
        return role;
    }

    public static Integer[] staffRoles() {
        return new Integer[]{ROLE_CHEF, ROLE_ADMIN};
    }
}
