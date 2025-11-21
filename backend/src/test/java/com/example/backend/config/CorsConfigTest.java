package com.example.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 1. ระบุ 'controllers' เพื่อบอก Spring ว่าให้โหลดเฉพาะ TestController
@WebMvcTest(controllers = CorsConfigTest.TestController.class)
// 2. ⭐️⭐️⭐️ FIX ⭐️⭐️⭐️
//    Import ทั้ง CorsConfig และ TestController
//    เพื่อสร้าง Bean ของ Controller นี้ใน Context
@Import({CorsConfig.class, CorsConfigTest.TestController.class})
public class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc; // 3. ตัวจำลองการยิง HTTP request

    // 4. Controller จำลอง (ตอนนี้ Spring Test จะรู้จักมันแล้ว)
    @RestController
    static class TestController {
        @GetMapping("/api/test")
        public String testEndpoint() {
            return "ok";
        }
    }

    @Test
    @DisplayName("ควรอนุญาต Request จาก Origin ที่กำหนดไว้ (localhost:5173)")
    public void whenRequestFromAllowedOrigin_thenReturnsAllowOriginHeader() throws Exception {
        String allowedOrigin = "http://localhost:5173";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/test")
                        .header(HttpHeaders.ORIGIN, allowedOrigin))
                // ตอนนี้ควรจะได้ 200 OK แล้ว
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin));
    }

    @Test
    @DisplayName("ควรอนุญาต Request จาก Origin ที่เป็น IP ที่กำหนดไว้ (IP fah)")
    public void whenRequestFromAnotherAllowedOrigin_thenReturnsAllowOriginHeader() throws Exception {
        String allowedIpOrigin = "http://192.168.43.60:30080";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/test")
                        .header(HttpHeaders.ORIGIN, allowedIpOrigin))
                // ตอนนี้ควรจะได้ 200 OK แล้ว
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedIpOrigin));
    }

    @Test
    @DisplayName("ควรปฏิเสธ Request (Forbidden) จาก Origin ที่ไม่ได้รับอนุญาต")
    public void whenRequestFromDisallowedOrigin_thenReturnsForbidden() throws Exception {
        String disallowedOrigin = "http://untrusted-site.com";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/test")
                        .header(HttpHeaders.ORIGIN, disallowedOrigin))
                // Test นี้ผ่านอยู่แล้ว เพราะ Filter ทำงานก่อน
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("ควรตอบกลับ Preflight (OPTIONS) Request อย่างถูกต้อง")
    public void whenOptionsRequestFromAllowedOrigin_thenReturnsCorrectHeaders() throws Exception {
        String allowedOrigin = "http://localhost:5173";

        mockMvc.perform(MockMvcRequestBuilders.options("/api/test")
                        .header(HttpHeaders.ORIGIN, allowedOrigin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                // Test นี้ผ่านอยู่แล้ว เพราะ Filter ทำงานก่อน
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin))
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE"));
    }
}