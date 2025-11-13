// src/main/java/com/example/backend/repository/BillParticipantRepository.java
package com.example.backend.repository;

import com.example.backend.model.BillParticipant;
import com.example.backend.model.BillParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BillParticipantRepository extends JpaRepository<BillParticipant, BillParticipantId> {

    List<BillParticipant> findByBillBillId(Integer billId);

    // ✅ เพิ่มเมธอดนี้เข้าไป
    Optional<BillParticipant> findByBillBillIdAndUserUserId(Integer billId, Integer userId);

    // เพิ่มเมธอดนี้ด้วย (สำหรับอีกไฟล์เทส)
    List<BillParticipant> findByUserUserIdAndBillGroupGroupId(Integer userId, Integer groupId);

    // คำนวณ "ยอดรวมที่ User คนนี้ต้องจ่าย (ส่วนแบ่ง)"
    // (เราถือว่า splitAmount ถูกบันทึกเป็น THB แล้ว)
    @Query("SELECT COALESCE(SUM(bp.splitAmount), 0) FROM BillParticipant bp WHERE bp.bill.group.groupId = :groupId AND bp.user.userId = :userId")
    BigDecimal sumTotalOwedByUserInGroup(@Param("userId") Integer userId, @Param("groupId") Integer groupId);

}