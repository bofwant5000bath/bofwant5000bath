package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "bill_participants")
@Getter
@Setter
public class BillParticipant implements Serializable {

    @EmbeddedId
    private BillParticipantId id;

    @ManyToOne
    @MapsId("billId")
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "split_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal splitAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_paid", nullable = false, columnDefinition = "ENUM('unpaid','partial','paid') default 'unpaid'")
    private PaymentStatus isPaid;
}
