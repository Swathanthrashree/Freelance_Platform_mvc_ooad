package com.freelance.controller;

import com.freelance.model.User;
import java.util.List;

public class UserController {
    private User userModel;

    public UserController() {
        this.userModel = new User();
    }

    public boolean registerUser(String username, String email, String password, String role) {
        User newUser = new User(username, email, password, role);
        return newUser.save();
    }

    public User loginUser(String email, String password) {
        User user = userModel.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userModel.findAll();
    }
} 