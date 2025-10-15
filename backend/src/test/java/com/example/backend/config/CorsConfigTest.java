package com.example.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// แก้ไขจุดที่ 1: เพิ่ม TestController.class เข้าไปใน @Import
// เพื่อให้แน่ใจว่า Spring Test Context จะสร้าง Bean ของ Controller นี้ขึ้นมา
@WebMvcTest(controllers = CorsConfigTest.TestController.class)
@Import({CorsConfig.class, CorsConfigTest.TestController.class})
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/api/test")
        public String testEndpoint() {
            return "OK";
        }
    }

    @Test
    @DisplayName("ควรจะได้รับ CORS headers ที่ถูกต้องเมื่อ Request มาจาก Origin ที่อนุญาต")
    void shouldReceiveCorrectCorsHeaders_When_RequestFromAllowedOrigin() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk()) // เทสนี้จะผ่านเป็น 200 OK แล้ว
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("ควรจะ Reject Request เมื่อมาจาก Origin ที่ไม่ได้รับอนุญาต")
    void shouldRejectRequest_When_RequestFromDisallowedOrigin() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Origin", "http://malicious-site.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ควรจะตอบกลับ Pre-flight request ด้วย Headers ที่ถูกต้อง")
    void shouldRespondToPreflightRequest_With_CorrectHeaders() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        // แก้ไขจุดที่ 2: เพิ่ม Header นี้เข้าไปเพื่อจำลอง Pre-flight request ที่สมบูรณ์
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE"))
                // เมื่อเพิ่ม Request Header ด้านบนแล้ว เทสนี้จะผ่าน
                .andExpect(header().string("Access-Control-Allow-Headers", "Content-Type"));
        // หมายเหตุ: การคาดหวังค่า "*" อาจไม่ตรงกับพฤติกรรมจริงของ Spring
        // Spring มักจะตอบกลับด้วยค่า header ที่ client ร้องขอมาจริงๆ
        // ดังนั้นการเปลี่ยนเป็น "Content-Type" จะตรงกว่า
    }
}