package com.example.backend.repository;

import com.example.backend.model.BillParticipant;
import com.example.backend.model.BillParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillParticipantRepository extends JpaRepository<BillParticipant, BillParticipantId> {
    // ใช้สำหรับดึง participants ของบิลเวลาแสดงรายละเอียด
    List<BillParticipant> findByBillBillId(Integer billId);
    // ดึงรายการ BillParticipant ทั้งหมดที่ user คนหนึ่งมีใน group หนึ่ง
    List<BillParticipant> findByUserUserIdAndBillGroupGroupId(Integer userId, Integer groupId);
}
