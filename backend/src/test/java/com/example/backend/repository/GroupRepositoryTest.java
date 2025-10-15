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
class GroupRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GroupRepository groupRepository;

    private User user1, user2, user3;
    private Group groupA, groupB;

    @BeforeEach
    void setUp() {
        // Arrange: สร้างข้อมูลตั้งต้นที่ซับซ้อนขึ้น
        user1 = new User(); user1.setUsername("u1"); user1.setPassword("p"); user1.setFullName("User 1"); entityManager.persist(user1);
        user2 = new User(); user2.setUsername("u2"); user2.setPassword("p"); user2.setFullName("User 2"); entityManager.persist(user2);
        user3 = new User(); user3.setUsername("u3"); user3.setPassword("p"); user3.setFullName("User 3"); entityManager.persist(user3);

        groupA = new Group(); groupA.setGroupName("Group A"); entityManager.persist(groupA);
        groupB = new Group(); groupB.setGroupName("Group B"); entityManager.persist(groupB);

        // Group A: มี user1, user2
        GroupMember gmA1 = new GroupMember(); gmA1.setGroup(groupA); gmA1.setUser(user1); entityManager.persist(gmA1);
        GroupMember gmA2 = new GroupMember(); gmA2.setGroup(groupA); gmA2.setUser(user2); entityManager.persist(gmA2);

        // Group B: มี user1, user3
        GroupMember gmB1 = new GroupMember(); gmB1.setGroup(groupB); gmB1.setUser(user1); entityManager.persist(gmB1);
        GroupMember gmB2 = new GroupMember(); gmB2.setGroup(groupB); gmB2.setUser(user3); entityManager.persist(gmB2);

        entityManager.flush();
    }

    @Test
    void findGroupsByUserId_shouldReturnCorrectGroupsForUser() {
        // Act
        List<Group> groupsForUser1 = groupRepository.findGroupsByUserId(user1.getUserId());
        List<Group> groupsForUser2 = groupRepository.findGroupsByUserId(user2.getUserId());

        // Assert
        assertEquals(2, groupsForUser1.size(), "User1 ควรจะอยู่ใน 2 กลุ่ม");
        assertTrue(groupsForUser1.stream().anyMatch(g -> g.getGroupName().equals("Group A")));
        assertTrue(groupsForUser1.stream().anyMatch(g -> g.getGroupName().equals("Group B")));

        assertEquals(1, groupsForUser2.size(), "User2 ควรจะอยู่ใน 1 กลุ่ม");
        assertEquals("Group A", groupsForUser2.get(0).getGroupName());
    }

    @Test
    void findUsersByGroupId_shouldReturnCorrectUsersInGroup() {
        // Act
        List<User> usersInGroupA = groupRepository.findUsersByGroupId(groupA.getGroupId());
        List<User> usersInGroupB = groupRepository.findUsersByGroupId(groupB.getGroupId());

        // Assert
        assertEquals(2, usersInGroupA.size(), "Group A ควรมีสมาชิก 2 คน");
        assertTrue(usersInGroupA.stream().anyMatch(u -> u.getUsername().equals("u1")));
        assertTrue(usersInGroupA.stream().anyMatch(u -> u.getUsername().equals("u2")));

        assertEquals(2, usersInGroupB.size(), "Group B ควรมีสมาชิก 2 คน");
        assertTrue(usersInGroupB.stream().anyMatch(u -> u.getUsername().equals("u1")));
        assertTrue(usersInGroupB.stream().anyMatch(u -> u.getUsername().equals("u3")));
    }
}