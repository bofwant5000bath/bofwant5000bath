package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // อ่านค่าจาก Env Variable ชื่อ FRONTEND_URL
    // ถ้าไม่มี (เช่นรัน Local) ให้ใช้ค่า Default เป็น http://localhost:5173
    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // แยก URL ด้วยเครื่องหมาย comma (,) กรณีมีหลาย Domain
                String[] allowedOrigins = frontendUrl.split(",");
                
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // เพิ่ม OPTIONS
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
