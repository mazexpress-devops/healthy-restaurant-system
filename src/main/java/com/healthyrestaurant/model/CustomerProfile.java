package com.healthyrestaurant.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class CustomerProfile {
    private final int tableNumber;
    private final int age;
    private final double weightKg;
    private final double heightCm;
    private final Goal goal;
    private final Set<HealthCondition> conditions;

    public CustomerProfile(
            int tableNumber,
            int age,
            double weightKg,
            double heightCm,
            Goal goal,
            Set<HealthCondition> conditions) {
        this.tableNumber = tableNumber;
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.goal = goal;
        this.conditions = conditions.isEmpty()
                ? EnumSet.noneOf(HealthCondition.class)
                : EnumSet.copyOf(conditions);
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getAge() {
        return age;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public Goal getGoal() {
        return goal;
    }

    public Set<HealthCondition> getConditions() {
        return Collections.unmodifiableSet(conditions);
    }

    public boolean hasCondition(HealthCondition condition) {
        return conditions.contains(condition);
    }
}
