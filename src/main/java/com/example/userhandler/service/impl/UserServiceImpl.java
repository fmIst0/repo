package com.example.userhandler.service.impl;

import com.example.userhandler.exception.UserBadRequestException;
import com.example.userhandler.exception.UserNotFoundException;
import com.example.userhandler.model.User;
import com.example.userhandler.resource.Users;
import com.example.userhandler.service.UserService;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserServiceImpl implements UserService {
    @Value("${minAgeAllowed}")
    private int minAgeAllowed;

    @Override
    public User register(User newUser) {
        int userAge = calculateAge(newUser.getBirthDate());
        if (!checkIfAgeAllowed(userAge)) {
            throw new UserBadRequestException("Age: " + userAge
                    + " is not allowed. You must be at least "
                    + minAgeAllowed + ".");
        }
        Users.storage.add(newUser);
        return newUser;
    }

    @Override
    public List<User> findAll() {
        return Users.storage;
    }

    @Override
    public User updateUpdate(String email, User user) {
        User existingUser = getExistingUser(email);

        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getFirstName() != null) {
            existingUser.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            existingUser.setLastName(user.getLastName());
        }
        if (user.getBirthDate() != null) {
            existingUser.setBirthDate(user.getBirthDate());
        }
        if (user.getAddress() != null) {
            existingUser.setAddress(user.getAddress());
        }
        if (user.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }

        return existingUser;
    }

    @Override
    public void deleteUser(String email) {
        boolean userDeleted = Users.storage.removeIf(u -> u.getEmail().equals(email));
        if (!userDeleted) {
            throw new UserNotFoundException("There is no user with email: " + email);
        }
    }

    @Override
    public List<User> getUsersByBirthDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("[from] date must be before [to] date");
        }

        return Users.storage.stream()
                .filter(user -> !user.getBirthDate().isBefore(from) && !user.getBirthDate().isAfter(to))
                .collect(Collectors.toList());
    }


    private boolean checkIfAgeAllowed(int age) {
        return age >= minAgeAllowed;
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private User getExistingUser(String email) {
        return Users.storage.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("There is no user with email: " + email));
    }
}
