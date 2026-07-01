package com.healthyrestaurant.service;

import com.healthyrestaurant.dao.UserDao;
import com.healthyrestaurant.model.User;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> login(String username, String password, int requiredRole) throws SQLException {
        Optional<User> user = userDao.findActiveByCredentials(username, password);
        if (!user.isPresent()) {
            return Optional.empty();
        }

        if (user.get().getRole() == requiredRole || user.get().getRole() == User.ROLE_ADMIN) {
            return user;
        }
        return Optional.empty();
    }
}
