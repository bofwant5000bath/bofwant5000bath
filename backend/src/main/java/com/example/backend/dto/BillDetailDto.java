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
    // ✅ ---- START: เพิ่มฟิลด์ใหม่ ----
    private String currencyCode;
    private BigDecimal exchangeRate;
    private BigDecimal amountInThb; // ⭐️ เราส่งฟิลด์ที่ DB คำนวณให้กลับไปด้วย
    private String promptpayNumber;
    private String receiptImageUrl;
    // ✅ ---- END: เพิ่มฟิลด์ใหม่ ----
    private UserDto paidByUser;
    private String splitMethod;
    private LocalDateTime billDate;
    private List<BillParticipantDto> participants;

    public BillDetailDto(Bill bill, List<BillParticipant> participants) {
        this.billId = bill.getBillId();
        this.title = bill.getTitle();
        this.description = bill.getDescription();
        this.amount = bill.getAmount();
        // ✅ ---- START: เพิ่ม set-ters ใน Constructor ----
        this.currencyCode = bill.getCurrencyCode();
        this.exchangeRate = bill.getExchangeRate();
        this.amountInThb = bill.getAmount().multiply(bill.getExchangeRate());
        this.promptpayNumber = bill.getPromptpayNumber();
        this.receiptImageUrl = bill.getReceiptImageUrl();
        // ✅ ---- END: เพิ่ม set-ters ใน Constructor ----
        this.paidByUser = new UserDto(bill.getPaidByUser());
        this.splitMethod = bill.getSplitMethod().toString();
        this.billDate = bill.getBillDate();
        this.participants = participants.stream()
                .map(BillParticipantDto::new)
                .collect(Collectors.toList());
    }
}
