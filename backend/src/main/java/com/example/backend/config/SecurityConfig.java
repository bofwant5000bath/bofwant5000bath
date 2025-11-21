package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔹 ปิด CSRF สำหรับ dev หรือจัดการ CSRF token ใน production
            .csrf(csrf -> csrf.disable())
            
            // 🔹 เปิดใช้งาน CORS และใช้ config จาก corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 🔹 ตั้งค่า session in-memory
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // 🔹 กำหนดสิทธิ์การเข้าถึง
            .authorizeHttpRequests(auth -> auth
                // public endpoints
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                // อื่นๆ ต้อง authenticated
                .anyRequest().authenticated()
            )

            // 🔹 สำหรับการ login/logout หากต้องการ future
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔹 เพิ่มโดเมน frontend ที่ต้องการเชื่อมต่อ
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "https://bofwant5000bath.zeabur.app",
            "http://localhost:30080",
            "http://192.168.43.60:30080",
            "http://192.168.43.227:30080",
            "http://172.20.10.2:30080"
        ));

        // 🔹 อนุญาต method
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 🔹 อนุญาต header ทุกชนิด
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 🔹 สำคัญมาก! ต้องอนุญาตให้ส่ง Cookie
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // 🔹 Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
