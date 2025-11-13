package com.example.backend.repository;

import com.example.backend.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {
    List<Bill> findByGroupGroupId(Integer groupId);

    @Query("SELECT COALESCE(SUM(b.amountInThb), 0) FROM Bill b WHERE b.group.groupId = :groupId")
    BigDecimal sumAmountByGroupId(@Param("groupId") Integer groupId);

    // ดึงบิลทั้งหมดในกลุ่มที่จ่ายโดย user คนหนึ่ง
    List<Bill> findByGroupGroupIdAndPaidByUserUserId(Integer groupId, Integer userId);

    // ✅ เพิ่มเมธอดนี้
    // คำนวณ "ยอดรวมที่ User คนนี้จ่ายให้กลุ่ม" (คิดเป็น THB)
    @Query("SELECT SUM(b.amount * b.exchangeRate) FROM Bill b WHERE b.group.groupId = :groupId AND b.paidByUser.userId = :userId")
    BigDecimal sumTotalPaidByUserInGroup(@Param("userId") Integer userId, @Param("groupId") Integer groupId);
}
