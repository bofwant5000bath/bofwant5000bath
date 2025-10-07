package com.example.backend.dto;

import com.example.backend.model.Bill;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BillDto {
    private Integer billId;
    private String title;
    private String description;
    private BigDecimal amount;
    private UserDto paidByUser;
    private String splitMethod;
    private LocalDateTime billDate;

    public BillDto(Bill bill) {
        this.billId = bill.getBillId();
        this.title = bill.getTitle();
        this.description = bill.getDescription();
        this.amount = bill.getAmount();
        this.paidByUser = new UserDto(bill.getPaidByUser());
        this.splitMethod = bill.getSplitMethod().toString();
        this.billDate = bill.getBillDate();
    }
}
