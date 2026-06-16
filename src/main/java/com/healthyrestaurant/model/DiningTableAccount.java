package com.healthyrestaurant.model;

public class DiningTableAccount {
    private final int id;
    private final int tableNumber;
    private final String accountName;
    private final String status;
    private final boolean active;

    public DiningTableAccount(
            int id,
            int tableNumber,
            String accountName,
            String status,
            boolean active) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.accountName = accountName;
        this.status = status;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getStatus() {
        return status;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isOpen() {
        return "OPEN".equals(status);
    }

    public String getDisplayName() {
        return accountName + " - table " + tableNumber;
    }
}
