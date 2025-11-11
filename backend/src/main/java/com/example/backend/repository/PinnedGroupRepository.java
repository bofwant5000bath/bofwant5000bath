package com.example.backend.repository;

import com.example.backend.model.PinnedGroup;
import com.example.backend.model.PinnedGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PinnedGroupRepository extends JpaRepository<PinnedGroup, PinnedGroupId> {
    // Spring Data JPA จะเข้าใจเองว่าให้ค้นหาจาก field 'userId' ที่อยู่ใน 'id' (PinnedGroupId)
    List<PinnedGroup> findByIdUserId(Integer userId);
}