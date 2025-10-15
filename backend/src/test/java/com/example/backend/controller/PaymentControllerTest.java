package com.example.backend.controller;

import com.example.backend.dto.CreatePaymentRequest;
import com.example.backend.dto.PaymentDto;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private Payment mockPayment;
    private CreatePaymentRequest createPaymentRequest;
    private Bill mockBill;
    private PaymentDto mockPaymentDto;

    @BeforeEach
    void setUp() {
        User mockPayer = new User();
        mockPayer.setUserId(1);
        mockPayer.setUsername("keatikun");

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

        mockPaymentDto = new PaymentDto(mockPayment);
    }

    @Test
    @DisplayName("GET /bill/{billId} - Success: Should return list of payments for a bill")
    void whenGetPaymentsByBill_withValidBillId_shouldReturnPaymentDtoList() throws Exception {
        Integer billId = 101;
        when(paymentService.getPaymentsByBillId(billId)).thenReturn(Collections.singletonList(mockPaymentDto));

        mockMvc.perform(get("/api/payments/bill/{billId}", billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].paymentId", is(1)))
                .andExpect(jsonPath("$[0].amount", is(150.0)))
                // ✅ FIX: Corrected the JSON path to look inside the nested object
                .andExpect(jsonPath("$[0].payerUser.username", is("keatikun")));
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