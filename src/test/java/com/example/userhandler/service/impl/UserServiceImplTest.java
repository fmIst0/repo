package com.example.userhandler.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.userhandler.exception.UserBadRequestException;
import com.example.userhandler.exception.UserNotFoundException;
import com.example.userhandler.model.User;
import com.example.userhandler.resource.Users;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private int minAgeAllowed;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        minAgeAllowed = 18;
        Users.storage.clear();
    }

    @AfterEach
    public void afterEach() {
        Users.storage.clear();
    }

    @Test
    @DisplayName("Verify register() method works")
    public void register_ValidUserRequest_ReturnsUser() {
        //Given
        User newUser = getUser();

        //When
        User savedUser = userService.register(newUser);

        //Then
        assertThat(savedUser).isEqualTo(newUser);
    }

    @Test
    @DisplayName("Verify the UserBadRequestException was thrown when age is less then 18")
    public void register_LessAgeThen18_ThrowsUserBadRequestException() {
        //Given
        ReflectionTestUtils.setField(userService, "minAgeAllowed", minAgeAllowed);
        User newUser = getUser();
        newUser.setBirthDate(LocalDate.of(2016, 1, 1));
        String expected =  "Age: " + calculateAge(newUser.getBirthDate())
                + " is not allowed. You must be at least "
                + minAgeAllowed + ".";

        //When
        UserBadRequestException exception = assertThrows(UserBadRequestException.class,
                () -> userService.register(newUser));

        //Then
        String actual = exception.getMessage();
        assertEquals(UserBadRequestException.class, exception.getClass());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify finAll() method works")
    public void findAll_ReturnsAllUsers() {
        //Given
        User user = getUser();
        List<User> expectedUsers = List.of(user);

        Users.storage.addAll(expectedUsers);

        //When
        List<User> actualUsers = userService.findAll();

        //Then
        assertEquals(expectedUsers, actualUsers);
    }

    @Test
    @DisplayName("Verify updateUser() method works")
    public void updateUser_ValidUser_ReturnsValidUser() {
        //Given
        User expected = getUser();
        Users.storage.add(expected);

        //When
        User actual = userService.updateUser(expected.getEmail(), expected);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("Verify UserNotFoundException was thrown when user does not exist")
    public void updateUser_UserDoesNotExist_ThrowsUserNotFoundException() {
        // Given
        User user = getUser();
        String expected = "There is no user with email: " + user.getEmail();

        // When
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(user.getEmail(), user));

        // Then
        String actual = exception.getMessage();
        assertEquals(UserNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify the UserBadRequestException was thrown when age is less then 18")
    public void updateUser_LessAgeThen18_ThrowsUserBadRequestException() {
        //Given
        ReflectionTestUtils.setField(userService, "minAgeAllowed", minAgeAllowed);
        User user = getUser();
        Users.storage.add(user);
        user.setBirthDate(LocalDate.of(2016, 1, 1));
        String expected =  "Age: " + calculateAge(user.getBirthDate())
                + " is not allowed. You must be at least "
                + minAgeAllowed + ".";

        //When
        UserBadRequestException exception = assertThrows(UserBadRequestException.class,
                () -> userService.updateUser(user.getEmail(), user));

        //Then
        String actual = exception.getMessage();
        assertEquals(UserBadRequestException.class, exception.getClass());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify deleteUser() method works")
    public void deleteUser_ExistingUser_UserDeletes() {
        //Given
        User user = getUser();
        Users.storage.add(user);

        //When
        userService.deleteUser(user.getEmail());

        //Then
        assertTrue(Users.storage.isEmpty());
    }

    @Test
    @DisplayName("Verify deleteUser() method throws UserNotFoundException if user does not exist")
    public void deleteUser_ExistingUser_ThrowsUserNotFoundException() {
        //Given
        User user = getUser();
        String expected = "There is no user with email: " + user.getEmail();

        //When
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(user.getEmail()));

        //Then
        String actual = exception.getMessage();
        assertEquals(UserNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getUsersByBirthDateRange() method works")
    public void getUsersByBirthDateRange_ValidFromTo_ReturnsUsers() {
        //Given
        LocalDate from = LocalDate.of(2005, 1, 1);
        LocalDate to = LocalDate.of(2005, 12, 31);

        User user = getUser().setBirthDate(LocalDate.of(2005, 5, 22));
        User user1 = getUser().setBirthDate(LocalDate.of(2005, 4, 22));
        User user2 = getUser().setBirthDate(LocalDate.of(2004, 5, 22));

        Users.storage.addAll(List.of(user, user1, user2));

        List<User> expected = List.of(user, user1);

        //When
        List<User> actual = userService.getUsersByBirthDateRange(from, to);

        //Then
        assertEquals(2, actual.size());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getUsersByBirthDateRange() method throws IllegalArgumentException when to before from")
    public void getUsersByBirthDateRange_ToBeforeFrom_ThrowsIllegalArgumentException() {
        //Given
        LocalDate from = LocalDate.of(2005, 1, 1);
        LocalDate to = LocalDate.of(2004, 12, 31);
        String expected = "[from] date must be before [to] date";

        //When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.getUsersByBirthDateRange(from, to));

        //Then
        String actual = exception.getMessage();
        assertEquals(IllegalArgumentException.class, exception.getClass());
        assertEquals(expected, actual);
    }

    private User getUser(){
        return new User()
                .setEmail("user@email.com")
                .setFirstName("Name")
                .setLastName("Surname")
                .setBirthDate(LocalDate.of(2000, 1,1))
                .setAddress("Address")
                .setPhoneNumber("123456789");
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}