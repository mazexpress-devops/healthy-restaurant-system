package com.healthyrestaurant.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public final class MongoDatabase {
    private static final String DEFAULT_URI = "mongodb://127.0.0.1:27017";
    private static final String DEFAULT_DATABASE = "healthy_restaurant";
    private static final MongoClient CLIENT = MongoClients.create(value("MONGO_URI", DEFAULT_URI));

    private MongoDatabase() {
    }

    public static com.mongodb.client.MongoDatabase getDatabase() {
        return CLIENT.getDatabase(value("MONGO_DB", DEFAULT_DATABASE));
    }

    private static String value(String environmentKey, String defaultValue) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.trim().isEmpty()) {
            return environmentValue.trim();
        }
        return defaultValue;
    }
}
