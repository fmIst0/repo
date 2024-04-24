package com.example.userhandler.service;

import com.example.userhandler.model.User;
import java.time.LocalDate;
import java.util.List;

public interface UserService {
    User register(User newUser);

    List<User> findAll();

    User updateUpdate(String email, User partialUpdate);

    void deleteUser(String email);

    List<User> getUsersByBirthDateRange(LocalDate from, LocalDate to);
}
