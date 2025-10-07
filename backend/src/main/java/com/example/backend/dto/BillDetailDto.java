package com.example.backend.dto;

import com.example.backend.model.Bill;
import com.example.backend.model.BillParticipant;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class BillDetailDto {
    private Integer billId;
    private String title;
    private String description;
    private BigDecimal amount;
    private UserDto paidByUser;
    private String splitMethod;
    private LocalDateTime billDate;
    private List<BillParticipantDto> participants;

    public BillDetailDto(Bill bill, List<BillParticipant> participants) {
        this.billId = bill.getBillId();
        this.title = bill.getTitle();
        this.description = bill.getDescription();
        this.amount = bill.getAmount();
        this.paidByUser = new UserDto(bill.getPaidByUser());
        this.splitMethod = bill.getSplitMethod().toString();
        this.billDate = bill.getBillDate();
        this.participants = participants.stream()
                .map(BillParticipantDto::new)
                .collect(Collectors.toList());
    }
}
