package com.example.backend.service;

import com.example.backend.model.Group;
import com.example.backend.model.User;
import com.example.backend.repository.GroupRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void registerNewUser_whenUsernameIsUnique_shouldSaveAndReturnUser() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User registeredUser = userService.registerNewUser("newuser", "password", "New User", null);

        // Assert
        assertNotNull(registeredUser);
        assertEquals("newuser", registeredUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class)); // Verify save was called
    }

    @Test
    void registerNewUser_whenUsernameExists_shouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("existinguser")).thenReturn(new User());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerNewUser("existinguser", "password", "Existing User", null);
        });
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Verify save was NOT called
    }

    @Test
    void login_whenCredentialsAreCorrect_shouldReturnUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsernameAndPassword("testuser", "password")).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.login("testuser", "password");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void getGroupsForUser_shouldReturnListOfGroups() {
        // Arrange
        when(groupRepository.findGroupsByUserId(1)).thenReturn(Collections.singletonList(new Group()));

        // Act
        List<Group> groups = userService.getGroupsForUser(1);

        // Assert
        assertEquals(1, groups.size());
        verify(groupRepository, times(1)).findGroupsByUserId(1);
    }
}