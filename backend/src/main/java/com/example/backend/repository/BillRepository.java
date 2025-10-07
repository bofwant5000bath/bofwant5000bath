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

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.group.groupId = :groupId")
    BigDecimal sumAmountByGroupId(@Param("groupId") Integer groupId);
}
