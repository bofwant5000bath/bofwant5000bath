package com.example.backend.service;

import com.example.backend.dto.CreatePaymentRequest;
import com.example.backend.dto.PaymentDto;
import com.example.backend.model.*;
import com.example.backend.repository.BillRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List; // เพิ่ม import
import java.util.Optional;
import java.util.stream.Collectors; // เพิ่ม import

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          BillRepository billRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }

    public List<PaymentDto> getPaymentsByBillId(Integer billId) {
        List<Payment> payments = paymentRepository.findByBillIdWithDetails(billId);
        return payments.stream()
                .map(PaymentDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        Optional<Bill> billOpt = billRepository.findById(request.getBillId());
        Optional<User> userOpt = userRepository.findById(request.getPayerUserId());

        if (billOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Bill or User not found");
        }

        Payment payment = new Payment();
        payment.setBill(billOpt.get());
        payment.setPayerUser(userOpt.get());
        payment.setAmount(request.getAmount());

        payment.setSlipImageUrl(request.getSlipImageUrl());

        return paymentRepository.save(payment);
    }
}
