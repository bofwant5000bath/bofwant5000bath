// src/main/java/com/example/backend/repository/BillParticipantRepository.java
package com.example.backend.repository;

import com.example.backend.model.BillParticipant;
import com.example.backend.model.BillParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BillParticipantRepository extends JpaRepository<BillParticipant, BillParticipantId> {

    List<BillParticipant> findByBillBillId(Integer billId);

    // ✅ เพิ่มเมธอดนี้เข้าไป
    Optional<BillParticipant> findByBillBillIdAndUserUserId(Integer billId, Integer userId);

    // เพิ่มเมธอดนี้ด้วย (สำหรับอีกไฟล์เทส)
    List<BillParticipant> findByUserUserIdAndBillGroupGroupId(Integer userId, Integer groupId);
}