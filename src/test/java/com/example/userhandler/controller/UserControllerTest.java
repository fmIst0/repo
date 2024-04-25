package com.example.userhandler.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.userhandler.model.User;
import com.example.userhandler.resource.Users;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext webApplicationContext
            ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @BeforeEach
    public void setUp() {
        Users.storage.clear();
    }

    @AfterEach
    public void afterEach() {
        Users.storage.clear();
    }

    @Test
    @DisplayName("Register user")
    void register_ValidUserRequest_ReturnsUser() throws Exception {
        //Given
        User newUser = getUser();

        //When
        MvcResult result = mockMvc.perform(post("/api/users")
                        .content(objectMapper.writeValueAsString(newUser))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        User actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                User.class);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(newUser, actual);
    }

    @Test
    @DisplayName("Register user invalid birthDate")
    void register_InvalidUserRequest_BadRequest() throws Exception {
        //Given
        User newUser = getUser().setBirthDate(LocalDate.of(2016, 1, 1));

        //When
        MvcResult result = mockMvc.perform(post("/api/users")
                        .content(objectMapper.writeValueAsString(newUser))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Update user")
    void updateUser_ValidUserRequest_ReturnsUpdatedUser() throws Exception {
        //Given
        User user = getUser();
        Users.storage.add(user);

        //When
        MvcResult result = mockMvc.perform(put("/api/users/" + user.getEmail())
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        User actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                User.class);
        EqualsBuilder.reflectionEquals(user, actual);
    }

    @Test
    @DisplayName("Update user does not exist")
    void updateUser_UserDoesNotExist_NotFound() throws Exception {
        //Given
        User user = getUser();

        //When
        MvcResult result = mockMvc.perform(put("/api/users/" + user.getEmail())
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    @DisplayName("Update user invalid request")
    void updateUser_InvalidRequest_BadRequest() throws Exception {
        //Given
        User user = getUser();
        Users.storage.add(user);
        User updateUser = user.setBirthDate(LocalDate.of(2010, 1, 1));

        //When
        MvcResult result = mockMvc.perform(put("/api/users/" + user.getEmail())
                        .content(objectMapper.writeValueAsString(updateUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Get users by birth date range")
    void getUsersByBirthDateRange_ToBeforeFrom_BadRequest() throws Exception {
        // Given
        LocalDate fromDate = LocalDate.of(1999, 1, 1);
        LocalDate toDate = LocalDate.of(1980, 1, 1);

        // When
        String url = "/api/users/search?from=" + fromDate + "&to=" + toDate;
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Get users by birth date range")
    void getUsersByBirthDateRange_ReturnsUsersInDateRange() throws Exception {
        // Given
        User user1 = getUser().setBirthDate(LocalDate.of(2000, 1, 1));
        User user2 = getUser().setBirthDate(LocalDate.of(1995, 6, 15));
        Users.storage.add(user1);
        Users.storage.add(user2);

        // When
        LocalDate fromDate = LocalDate.of(1999, 1, 1);
        LocalDate toDate = LocalDate.of(2001, 1, 1);
        String url = "/api/users/search?from=" + fromDate + "&to=" + toDate;
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<User> users = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<User>>() {});
        assertEquals(1, users.size());
        EqualsBuilder.reflectionEquals(user1, users.get(0));
    }

    @Test
    @DisplayName("Get all users")
    void getAll_ReturnsAllUsers() throws Exception {
        // Given
        User user1 = getUser().setBirthDate(LocalDate.of(2000, 1, 1));
        User user2 = getUser().setBirthDate(LocalDate.of(1995, 6, 15));
        Users.storage.add(user1);
        Users.storage.add(user2);

        //When
        MvcResult result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        //Then
        List<User> users = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<User>>() {});
        assertEquals(Users.storage.size(), users.size());
        assertIterableEquals(Users.storage, users);
    }

    @Test
    @DisplayName("Delete existing user")
    void deleteUser_UserExists_isAccepted() throws Exception {
        //Given
        User user = getUser();
        Users.storage.add(user);

        //When
        MvcResult result = mockMvc.perform(delete("/api/users/" + user.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
    }

    @Test
    @DisplayName("Delete user does not exist")
    void deleteUser_UserDoesNotExist_NotFound() throws Exception {
        //Given
        User user = getUser();

        //When
        MvcResult result = mockMvc.perform(delete("/api/users/" + user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
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
}