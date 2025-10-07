package com.example.backend.controller;

import com.example.backend.dto.CreatePaymentRequest;
import com.example.backend.model.Bill;
import com.example.backend.model.Payment;
import com.example.backend.model.User;
import com.example.backend.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private Payment mockPayment;
    private CreatePaymentRequest createPaymentRequest;
    private Bill mockBill;

    @BeforeEach
    void setUp() {
        User mockPayer = new User();
        mockPayer.setUserId(1);

        mockBill = new Bill();
        mockBill.setBillId(101);

        mockPayment = new Payment();
        mockPayment.setPaymentId(1);
        mockPayment.setBill(mockBill);
        mockPayment.setPayerUser(mockPayer);
        mockPayment.setAmount(new BigDecimal("150.00"));
        mockPayment.setPaymentDate(LocalDateTime.now());

        createPaymentRequest = new CreatePaymentRequest();
        createPaymentRequest.setBillId(101);
        createPaymentRequest.setPayerUserId(1);
        createPaymentRequest.setAmount(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("POST /create - Success: Should create a new payment and return it")
    void whenCreatePayment_withValidRequest_thenReturnsCreated() throws Exception {
        when(paymentService.createPayment(any(CreatePaymentRequest.class))).thenReturn(mockPayment);

        mockMvc.perform(post("/api/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(1))
                // ✅ FIX: Navigate into the nested "bill" object to find "billId"
                .andExpect(jsonPath("$.bill.billId").value(101))
                .andExpect(jsonPath("$.amount").value(150.00));
    }

    @Test
    @DisplayName("POST /create - Failure: Should return Bad Request when request data is invalid")
    void whenCreatePayment_withInvalidRequest_thenReturnsBadRequest() throws Exception {
        when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid bill or user ID"));

        mockMvc.perform(post("/api/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequest)))
                .andExpect(status().isBadRequest());
    }
}