package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "paid_by_user_id", nullable = false)
    private User paidByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_method", nullable = false)
    private SplitMethod splitMethod;  // ✅ ใช้ enum ที่อยู่ไฟล์แยก

    @Column(name = "bill_date", nullable = false)
    private LocalDateTime billDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

