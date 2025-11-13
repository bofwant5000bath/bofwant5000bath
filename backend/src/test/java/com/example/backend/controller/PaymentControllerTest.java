package com.example.backend.controller;

import com.example.backend.dto.CreatePaymentRequest;
import com.example.backend.dto.PaymentDto;
import com.example.backend.model.Bill;
import com.example.backend.model.Payment;
import com.example.backend.model.User;
import com.example.backend.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime; // ⭐️ อาจจะต้อง Import
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("GET /bill/{billId} ควรคืนค่า 200 OK และ List<PaymentDto>")
    void getPaymentsByBill_shouldReturn200_withPaymentList() throws Exception {
        // Arrange
        Integer billId = 1;

        User mockPayer = new User();
        mockPayer.setUserId(101);
        mockPayer.setFullName("Payer Name");

        Bill mockBill = new Bill();
        mockBill.setBillId(billId);

        Payment mockPaymentEntity = new Payment();
        mockPaymentEntity.setPaymentId(1);
        mockPaymentEntity.setAmount(new BigDecimal("100.00"));
        mockPaymentEntity.setPayerUser(mockPayer);
        mockPaymentEntity.setBill(mockBill);
        mockPaymentEntity.setPaymentDate(LocalDateTime.now()); // (เพิ่มการ set วันที่ เผื่อ DTO ใช้)

        PaymentDto mockPaymentDto = new PaymentDto(mockPaymentEntity);
        List<PaymentDto> mockList = List.of(mockPaymentDto);

        when(paymentService.getPaymentsByBillId(billId)).thenReturn(mockList);

        // Act & Assert
        mockMvc.perform(get("/api/payments/bill/" + billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                // ⭐️⭐️⭐️ FIX 1: แก้ JsonPath ⭐️⭐️⭐️
                .andExpect(jsonPath("$[0].payerUser.fullName").value("Payer Name"))
                .andExpect(jsonPath("$[0].amount").value(100.00));
    }

    @Test
    @DisplayName("POST /create ควรคืนค่า 201 Created และ PaymentDto ที่สร้างใหม่")
    void createPayment_shouldReturn201_withCreatedPaymentDto() throws Exception {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setBillId(1);
        request.setAmount(new BigDecimal("50.00"));
        request.setPayerUserId(101);

        User mockPayer = new User();
        mockPayer.setUserId(101);
        mockPayer.setFullName("Payer Name");

        Bill mockBill = new Bill();
        mockBill.setBillId(1);

        Payment mockPaymentEntity = new Payment();
        mockPaymentEntity.setPaymentId(99);
        mockPaymentEntity.setAmount(new BigDecimal("50.00"));
        mockPaymentEntity.setPayerUser(mockPayer);
        mockPaymentEntity.setBill(mockBill);
        mockPaymentEntity.setPaymentDate(LocalDateTime.now()); // (เพิ่มการ set วันที่ เผื่อ DTO ใช้)

        when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                .thenReturn(mockPaymentEntity);

        // Act & Assert
        mockMvc.perform(post("/api/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(99))
                // ⭐️⭐️⭐️ FIX 2: แก้ JsonPath ⭐️⭐️⭐️
                .andExpect(jsonPath("$.payerUser.fullName").value("Payer Name"));
    }
}