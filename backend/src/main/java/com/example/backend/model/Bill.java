package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "bills")
@Getter
@Setter
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Integer billId;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    // ✅ ---- START: เพิ่มฟิลด์ใหม่ ----
    @Column(name = "currency_code")
    private String currencyCode;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    // ⭐️ ฟิลด์นี้ DB จะคำนวณให้ เราจึงตั้งค่า insertable=false, updatable=false
    @Column(name = "amount_in_thb", insertable = false, updatable = false)
    private BigDecimal amountInThb;

    @ManyToOne
    @JoinColumn(name = "paid_by_user_id", nullable = false)
    private User paidByUser;

    @Column(name = "promptpay_number")
    private String promptpayNumber;

    @Column(name = "receipt_image_url")
    private String receiptImageUrl;
    // ✅ ---- END: เพิ่มฟิลด์ใหม่ ----

    @Enumerated(EnumType.STRING)
    @Column(name = "split_method", nullable = false)
    private SplitMethod splitMethod;  // ✅ ใช้ enum ที่อยู่ไฟล์แยก

    @Column(name = "bill_date", nullable = false)
    private LocalDateTime billDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ✅ 2. เพิ่มส่วนนี้เข้าไปทั้งหมด
    @OneToMany(mappedBy = "bill", fetch = FetchType.LAZY)
    private List<BillParticipant> participants;
}

