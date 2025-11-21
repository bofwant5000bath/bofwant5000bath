package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // อ่านค่าจาก Env Variable (ใน Zeabur)
    // ถ้าไม่มี ให้ใช้ * (อนุญาตทั้งหมด)
    @Value("${FRONTEND_URL:*}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // ✅ แก้จุดตาย: ใช้ allowedOriginPatterns แทน allowedOrigins
                        // เพื่อให้ใช้ "*" คู่กับ allowCredentials(true) ได้โดยไม่ Error
                        .allowedOriginPatterns(frontendUrl.split(","))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true); // อนุญาตให้ส่ง Cookies/Auth Headers
            }
        };
    }
}
