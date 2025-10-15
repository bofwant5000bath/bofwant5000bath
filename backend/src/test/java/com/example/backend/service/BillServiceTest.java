package com.example.backend.service;

import com.example.backend.dto.CreateBillRequest;
import com.example.backend.model.*;
import com.example.backend.repository.*;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock private BillRepository billRepository;
    @Mock private BillParticipantRepository billParticipantRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks
    private BillService billService;

    @Test
    void createBill_withEqualSplit_shouldCreateParticipantsAndPaymentForPayer() {
        // Arrange
        User payer = new User();
        payer.setUserId(1);
        User participant = new User();
        participant.setUserId(2);

        CreateBillRequest request = new CreateBillRequest();
        request.setGroupId(10);
        request.setPaidByUserId(1);
        request.setAmount(new BigDecimal("100.00"));
        request.setSplitMethod(SplitMethod.equal);

        Bill savedBill = new Bill();
        savedBill.setBillId(101);

        when(groupRepository.findById(10)).thenReturn(Optional.of(new Group()));
        when(userRepository.findById(1)).thenReturn(Optional.of(payer));
        when(groupRepository.findUsersByGroupId(10)).thenReturn(List.of(payer, participant));
        when(billRepository.save(any(Bill.class))).thenReturn(savedBill);

        // Act
        billService.createBill(request);

        // Assert
        // Verify 2 participants were saved (payer and other participant)
        verify(billParticipantRepository, times(2)).save(any(BillParticipant.class));
        // Verify that ONLY ONE payment was created (for the person who paid)
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createBill_withUnequalSplit_shouldCreateParticipantsAndPayment() {
        // Arrange
        User payer = new User();
        payer.setUserId(1);
        User participantUser = new User();
        participantUser.setUserId(2);

        CreateBillRequest.ParticipantSplitDto splitDto = new CreateBillRequest.ParticipantSplitDto();
        splitDto.setUserId(2);
        splitDto.setAmount(new BigDecimal("40.00"));

        CreateBillRequest request = new CreateBillRequest();
        request.setGroupId(10);
        request.setPaidByUserId(1);
        request.setAmount(new BigDecimal("100.00"));
        request.setSplitMethod(SplitMethod.unequal);
        request.setParticipants(Collections.singletonList(splitDto));

        Bill savedBill = new Bill();
        savedBill.setBillId(101);

        when(groupRepository.findById(10)).thenReturn(Optional.of(new Group()));
        when(userRepository.findById(1)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2)).thenReturn(Optional.of(participantUser));
        when(billRepository.save(any(Bill.class))).thenReturn(savedBill);

        // Act
        billService.createBill(request);

        // Assert
        verify(billParticipantRepository, times(1)).save(any(BillParticipant.class));
        // In this specific unequal setup, no payment is created for the payer because they aren't in the split list
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createBill_whenGroupNotFound_shouldThrowException() {
        // Arrange
        CreateBillRequest request = new CreateBillRequest();
        request.setGroupId(99);
        when(groupRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> billService.createBill(request));
    }
}