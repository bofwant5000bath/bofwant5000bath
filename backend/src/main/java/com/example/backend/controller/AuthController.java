package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        return userService.login(username, password)
                .map(user -> ResponseEntity.ok(Map.of(
                        "message", "Login successful",
                        "user_id", user.getUserId(),
                        "username", user.getUsername(),
                        "full_name", user.getFullName()
                )))
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid credentials")));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam("full_name") String fullName,
                                           @RequestParam("username") String username,
                                           @RequestParam("password") String password,
                                           @RequestParam(value = "picture", required = false) MultipartFile picture) {
        try {
            // ในโปรเจกต์จริงควรมีการจัดการไฟล์อัปโหลดอย่างเหมาะสม เช่น บันทึกไฟล์ลงใน cloud storage
            String profilePictureUrl = (picture != null) ? "URL_TO_UPLOADED_PICTURE" : null;

            userService.registerNewUser(username, password, fullName, profilePictureUrl);

            return ResponseEntity.status(HttpStatus.CREATED).body("การลงทะเบียนสำเร็จ");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("เกิดข้อผิดพลาดในการลงทะเบียน");
        }
    }
}
