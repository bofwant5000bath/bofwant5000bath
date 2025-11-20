package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. ปิด CSRF เพื่อให้ Login ผ่าน API ได้
            .csrf(csrf -> csrf.disable())
            
            // 2. เปิด CORS โดยใช้ Config ด้านล่าง
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. ตั้งค่าการเข้าถึง URL
            .authorizeHttpRequests(auth -> auth
                // อนุญาตให้เข้า /api/auth/... และ /api/public/... ได้โดยไม่ต้อง Login
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                // หน้าอื่นๆ ต้อง Login ก่อน
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // อนุญาตให้ Frontend เข้าถึงได้ (ทั้ง Local และ Zeabur)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "https://bofwant5000bath.zeabur.app" // โดเมน Frontend ของคุณ
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // อนุญาตให้ส่ง Cookies/Auth Headers ได้

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
