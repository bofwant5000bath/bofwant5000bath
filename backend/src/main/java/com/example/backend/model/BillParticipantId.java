package com.example.backend.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class BillParticipantId implements Serializable {

    private Integer billId;
    private Integer userId;

    public BillParticipantId() {}

    public BillParticipantId(Integer billId, Integer userId) {
        this.billId = billId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BillParticipantId that)) return false;
        return Objects.equals(billId, that.billId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId, userId);
    }
}
