package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setFullName("Test User");
        testUser.setProfilePictureUrl("https://example.com/profile.jpg");
    }

    // ===== LOGIN TESTS =====

    @Test
    void login_WithValidCredentials_ReturnsSuccessResponse() throws Exception {
        // Arrange
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", "testuser");
        loginPayload.put("password", "password123");

        when(userService.login("testuser", "password123"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user_id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.full_name").value("Test User"))
                .andExpect(jsonPath("$.profile_picture_url").value("https://example.com/profile.jpg"));

        verify(userService, times(1)).login("testuser", "password123");
    }

    @Test
    void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", "wronguser");
        loginPayload.put("password", "wrongpass");

        when(userService.login("wronguser", "wrongpass"))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(userService, times(1)).login("wronguser", "wrongpass");
    }

    @Test
    void login_WithNullProfilePicture_ReturnsSuccessResponse() throws Exception {
        // Arrange
        testUser.setProfilePictureUrl(null);
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("username", "testuser");
        loginPayload.put("password", "password123");

        when(userService.login("testuser", "password123"))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.profile_picture_url").isEmpty());

        verify(userService, times(1)).login("testuser", "password123");
    }

    // ===== REGISTER TESTS =====

    @Test
    void register_WithValidData_ReturnsCreatedStatus() throws Exception {
        // Arrange
        User registerRequest = new User();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("newpass123");
        registerRequest.setFullName("New User");
        registerRequest.setProfilePictureUrl("https://example.com/new.jpg");

        when(userService.registerNewUser("newuser", "newpass123", "New User", "https://example.com/new.jpg"))
                .thenReturn(testUser); // หรือ return อะไรก็ได้ตาม return type ของ method

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("การลงทะเบียนสำเร็จ"));

        verify(userService, times(1))
                .registerNewUser("newuser", "newpass123", "New User", "https://example.com/new.jpg");
    }

    @Test
    void register_WithNullProfilePicture_ReturnsCreatedStatus() throws Exception {
        // Arrange
        User registerRequest = new User();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("newpass123");
        registerRequest.setFullName("New User");
        registerRequest.setProfilePictureUrl(null);

        when(userService.registerNewUser("newuser", "newpass123", "New User", null))
                .thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("การลงทะเบียนสำเร็จ"));

        verify(userService, times(1))
                .registerNewUser("newuser", "newpass123", "New User", null);
    }

    @Test
    void register_WithExistingUsername_ReturnsConflict() throws Exception {
        // Arrange
        User registerRequest = new User();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Existing User");

        doThrow(new IllegalArgumentException("ชื่อผู้ใช้นี้ถูกใช้งานแล้ว"))
                .when(userService)
                .registerNewUser(eq("existinguser"), anyString(), anyString(), any());

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("ชื่อผู้ใช้นี้ถูกใช้งานแล้ว"));

        verify(userService, times(1))
                .registerNewUser(eq("existinguser"), anyString(), anyString(), any());
    }

    @Test
    void register_WithUnexpectedError_ReturnsInternalServerError() throws Exception {
        // Arrange
        User registerRequest = new User();
        registerRequest.setUsername("erroruser");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Error User");

        doThrow(new RuntimeException("Database error"))
                .when(userService)
                .registerNewUser(eq("erroruser"), anyString(), anyString(), any());

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("เกิดข้อผิดพลาดในการลงทะเบียน"));

        verify(userService, times(1))
                .registerNewUser(eq("erroruser"), anyString(), anyString(), any());
    }
}