package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value; // เพิ่ม import นี้
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // อ่านค่าจาก Env Var ชื่อ FRONTEND_URL ถ้าไม่มีให้ใช้ * (อนุญาตหมด)
    @Value("${FRONTEND_URL:*}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(frontendUrl.split(",")) // รองรับการใส่หลาย URL คั่นด้วย comma
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // เพิ่ม OPTIONS สำคัญมาก
                        .allowedHeaders("*")
                        .allowCredentials(true); // ถ้ามีการส่ง Cookie/Auth ต้องเปิดตัวนี้
            }
        };
    }
}
