package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
// ✅ แก้ไข: ไม่จำเป็นต้อง import doNothing() แล้ว
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        mockUser.setFullName("Test User");
        mockUser.setPassword("password");
    }

    @Test
    @DisplayName("POST /login - Success: Should return user details and OK status when credentials are valid")
    void whenLoginWithValidCredentials_thenReturnsSuccess() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password");

        when(userService.login(anyString(), anyString())).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user_id").value(mockUser.getUserId()))
                .andExpect(jsonPath("$.username").value(mockUser.getUsername()))
                .andExpect(jsonPath("$.full_name").value(mockUser.getFullName()));
    }

    @Test
    @DisplayName("POST /login - Failure: Should return Unauthorized status when credentials are invalid")
    void whenLoginWithInvalidCredentials_thenReturnsUnauthorized() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "wronguser");
        loginRequest.put("password", "wrongpassword");

        when(userService.login(anyString(), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /register - Success: Should return Created status for successful registration")
    void whenRegisterWithValidData_thenReturnsCreated() throws Exception {
        String fullName = "New User";
        String username = "newuser";
        String password = "newpassword";
        MockMultipartFile pictureFile = new MockMultipartFile("picture", "profile.jpg", "image/jpeg", "some-image-bytes".getBytes());

        // ✅ แก้ไข: เปลี่ยนจาก doNothing() เป็น when(...).thenReturn(...)
        // เนื่องจาก Controller ไม่ได้ใช้ค่าที่ return เราจึง return mockUser หรือ new User() ก็ได้
        when(userService.registerNewUser(anyString(), anyString(), anyString(), any())).thenReturn(mockUser);

        mockMvc.perform(multipart("/api/auth/register")
                        .file(pictureFile)
                        .param("full_name", fullName)
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isCreated())
                .andExpect(content().string("การลงทะเบียนสำเร็จ"));
    }

    @Test
    @DisplayName("POST /register - Conflict: Should return Conflict status when username already exists")
    void whenRegisterWithExistingUsername_thenReturnsConflict() throws Exception {
        String fullName = "Existing User";
        String username = "existinguser";
        String password = "password";
        String errorMessage = "Username " + username + " is already taken.";

        // ✅ แก้ไข: เปลี่ยนมาใช้ when(...).thenThrow(...) ซึ่งอ่านเข้าใจง่ายกว่าสำหรับ non-void method
        when(userService.registerNewUser(anyString(), anyString(), anyString(), any()))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(multipart("/api/auth/register")
                        .param("full_name", fullName)
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isConflict())
                .andExpect(content().string(errorMessage));
    }
}