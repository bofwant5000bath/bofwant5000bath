package com.example.backend.repository;

import com.example.backend.model.Group;
import com.example.backend.model.GroupMember;
import com.example.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GroupMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private User user1, user2;
    private Group group1, group2;

    @BeforeEach
    void setUp() {
        // Arrange: สร้างข้อมูลตั้งต้น
        user1 = new User(); user1.setUsername("u1"); user1.setPassword("p"); user1.setFullName("u1");
        entityManager.persist(user1);
        user2 = new User(); user2.setUsername("u2"); user2.setPassword("p"); user2.setFullName("u2");
        entityManager.persist(user2);

        group1 = new Group(); group1.setGroupName("G1");
        entityManager.persist(group1);
        group2 = new Group(); group2.setGroupName("G2");
        entityManager.persist(group2);

        // User1 อยู่ใน Group1 และ Group2
        GroupMember gm1 = new GroupMember(); gm1.setUser(user1); gm1.setGroup(group1);
        entityManager.persist(gm1);
        GroupMember gm2 = new GroupMember(); gm2.setUser(user1); gm2.setGroup(group2);
        entityManager.persist(gm2);

        // User2 อยู่ใน Group1 เท่านั้น
        GroupMember gm3 = new GroupMember(); gm3.setUser(user2); gm3.setGroup(group1);
        entityManager.persist(gm3);

        entityManager.flush();
    }

    @Test
    void findGroupsByUserId_shouldReturnCorrectGroups() {
        // Act
        List<Group> user1Groups = groupMemberRepository.findGroupsByUserId(user1.getUserId());
        List<Group> user2Groups = groupMemberRepository.findGroupsByUserId(user2.getUserId());

        // Assert
        assertEquals(2, user1Groups.size(), "User1 ควรอยู่ใน 2 กลุ่ม");
        assertEquals(1, user2Groups.size(), "User2 ควรอยู่ใน 1 กลุ่ม");
        assertEquals("G1", user2Groups.get(0).getGroupName());
    }

    @Test
    void countByGroupId_shouldReturnCorrectMemberCount() {
        // Act
        Integer group1MemberCount = groupMemberRepository.countByGroupId(group1.getGroupId());
        Integer group2MemberCount = groupMemberRepository.countByGroupId(group2.getGroupId());

        // Assert
        assertEquals(2, group1MemberCount, "Group1 ควรมีสมาชิก 2 คน");
        assertEquals(1, group2MemberCount, "Group2 ควรมีสมาชิก 1 คน");
    }
}