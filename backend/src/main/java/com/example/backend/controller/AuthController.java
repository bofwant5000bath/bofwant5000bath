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
@CrossOrigin(origins = "*") // 👈 เติมแค่อันนี้ครับ (อนุญาตให้ Frontend ทุกที่เข้าได้)
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
                .map(user -> {
                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("message", "Login successful");
                    response.put("user_id", user.getUserId());
                    response.put("username", user.getUsername());
                    response.put("full_name", user.getFullName());
                    response.put("profile_picture_url", user.getProfilePictureUrl()); 
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid credentials")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User userRequest) {
        try {
            String username = userRequest.getUsername();
            String password = userRequest.getPassword();
            String fullName = userRequest.getFullName();
            String profilePictureUrl = userRequest.getProfilePictureUrl();

            userService.registerNewUser(username, password, fullName, profilePictureUrl);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "การลงทะเบียนสำเร็จ"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "เกิดข้อผิดพลาดในการลงทะเบียน"));
        }
    }
}
