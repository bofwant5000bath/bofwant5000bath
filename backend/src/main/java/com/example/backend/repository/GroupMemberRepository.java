package com.example.backend.repository;

import com.example.backend.model.Group;
import com.example.backend.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {

    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user.userId = :userId")
    List<Group> findGroupsByUserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.groupId = :groupId")
    Integer countByGroupId(@Param("groupId") Integer groupId);
}
