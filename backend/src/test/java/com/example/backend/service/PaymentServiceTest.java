package com.example.backend.service;

import com.example.backend.dto.CreatePaymentRequest;
import com.example.backend.dto.PaymentDto;
import com.example.backend.model.Bill;
import com.example.backend.model.Payment;
import com.example.backend.model.User;
import com.example.backend.repository.BillRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_whenBillAndUserExist_shouldSavePayment() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setBillId(1);
        request.setPayerUserId(2);
        request.setAmount(new BigDecimal("100.00"));

        when(billRepository.findById(1)).thenReturn(Optional.of(new Bill()));
        when(userRepository.findById(2)).thenReturn(Optional.of(new User()));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payment createdPayment = paymentService.createPayment(request);

        // Assert
        assertNotNull(createdPayment);
        assertEquals(0, new BigDecimal("100.00").compareTo(createdPayment.getAmount()));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_whenBillNotFound_shouldThrowException() {
        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setBillId(99); // Non-existent bill
        when(billRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentsByBillId_shouldReturnPaymentDtos() {
        // Arrange
        User payer = new User();
        payer.setUserId(1);

        // ✅ **FIX: Create a mock Bill**
        Bill mockBill = new Bill();
        mockBill.setBillId(10); // The DTO needs the bill ID

        Payment payment = new Payment();
        payment.setPayerUser(payer);
        // ✅ **FIX: Set the mock Bill on the Payment object**
        payment.setBill(mockBill);

        when(paymentRepository.findByBillIdWithDetails(10)).thenReturn(Collections.singletonList(payment));

        // Act
        List<PaymentDto> paymentDtos = paymentService.getPaymentsByBillId(10);

        // Assert
        assertEquals(1, paymentDtos.size());
        assertEquals(10, paymentDtos.get(0).getBillId()); // You can now assert this
        verify(paymentRepository, times(1)).findByBillIdWithDetails(10);
    }
}