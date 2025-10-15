package com.example.backend.repository;

import com.example.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Arrange: Create a user and save it to the test database before each test
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setFullName("Test User");
        entityManager.persistAndFlush(user);
    }

    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        // Act
        User found = userRepository.findByUsername("testuser");

        // Assert
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
    }

    @Test
    void findByUsername_whenUserDoesNotExist_shouldReturnNull() {
        // Act
        User found = userRepository.findByUsername("nonexistent");

        // Assert
        assertNull(found);
    }

    @Test
    void findByUsernameAndPassword_whenCredentialsAreCorrect_shouldReturnUser() {
        // Act
        Optional<User> found = userRepository.findByUsernameAndPassword("testuser", "password123");

        // Assert
        assertTrue(found.isPresent(), "User should be found with correct credentials");
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void findByUsernameAndPassword_whenPasswordIsIncorrect_shouldReturnEmpty() {
        // Act
        Optional<User> found = userRepository.findByUsernameAndPassword("testuser", "wrongpassword");

        // Assert
        assertFalse(found.isPresent(), "User should not be found with incorrect password");
    }
}