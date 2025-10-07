package com.example.backend.repository;

import com.example.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // ✅ ยอดที่ userId จ่ายจริงใน group
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.payerUser.userId = :userId AND p.bill.group.groupId = :groupId")
    BigDecimal sumAmountByPayerUserIdAndGroupId(@Param("userId") Integer userId,
                                                @Param("groupId") Integer groupId);

    // ✅ ยอดที่เพื่อนจ่ายคืนให้เรา (ถ้าเราเป็นคนออกบิล)
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.bill.group.groupId = :groupId " +
            "AND p.bill.paidByUser.userId = :userId " +
            "AND p.payerUser.userId <> :userId")
    BigDecimal sumPaymentsFromOthersToMe(@Param("userId") Integer userId,
                                         @Param("groupId") Integer groupId);

    // ✅ portion ที่ userId ต้องจ่ายใน group
    @Query("SELECT COALESCE(SUM(bp.splitAmount), 0) " +
            "FROM BillParticipant bp " +
            "WHERE bp.user.userId = :userId " +
            "AND bp.bill.group.groupId = :groupId")
    BigDecimal sumExpectedForUser(@Param("userId") Integer userId,
                                  @Param("groupId") Integer groupId);

    // ✅ portion ที่เพื่อนต้องจ่ายให้เรา (ถ้าเราเป็นคนออกบิล)
    @Query("SELECT COALESCE(SUM(bp.splitAmount), 0) " +
            "FROM BillParticipant bp " +
            "WHERE bp.bill.group.groupId = :groupId " +
            "AND bp.bill.paidByUser.userId = :userId " +
            "AND bp.user.userId <> :userId")
    BigDecimal sumExpectedFromOthers(@Param("userId") Integer userId,
                                     @Param("groupId") Integer groupId);
}
