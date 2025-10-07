package com.example.backend.service;

import com.example.backend.model.Group;
import com.example.backend.model.User;
import com.example.backend.repository.GroupRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public UserService(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    public User registerNewUser(String username, String password, String fullName, String profilePictureUrl) {
        // Check if username already exists
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Create new user entity
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // In a real application, you should hash the password
        newUser.setFullName(fullName);
        newUser.setProfilePictureUrl(profilePictureUrl);
        newUser.setCreatedAt(LocalDateTime.now());

        // Save user to the database
        return userRepository.save(newUser);
    }

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }

    public List<Group> getGroupsForUser(Integer userId) {
        return groupRepository.findGroupsByUserId(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
