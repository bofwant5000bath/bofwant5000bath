package com.example.backend.controller;

import com.example.backend.dto.CreatePaymentRequest;
import com.example.backend.dto.PaymentDto; 
import com.example.backend.model.Payment;
import com.example.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; 

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*") // 👈 เติมตรงนี้ครับ
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/bill/{billId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByBill(@PathVariable Integer billId) {
        List<PaymentDto> payments = paymentService.getPaymentsByBillId(billId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentDto> createPayment(@RequestBody CreatePaymentRequest request) { 
        try {
            Payment payment = paymentService.createPayment(request);
            PaymentDto paymentDto = new PaymentDto(payment); 
            return new ResponseEntity<>(paymentDto, HttpStatus.CREATED); 
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
