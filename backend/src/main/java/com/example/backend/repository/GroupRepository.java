package com.example.backend.repository;

import com.example.backend.model.Group;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user.userId = :userId")
    List<Group> findGroupsByUserId(@Param("userId") Integer userId);

    // เพิ่มเมธอดนี้
    @Query("SELECT gm.user FROM GroupMember gm WHERE gm.group.groupId = :groupId")
    List<User> findUsersByGroupId(@Param("groupId") Integer groupId);
}
