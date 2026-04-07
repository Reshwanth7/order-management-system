package com.orderapp.user_service.service;

import com.orderapp.user_service.dto.CreateUserRequest;
import com.orderapp.user_service.dto.UserResponse;
import com.orderapp.user_service.entity.User;
import com.orderapp.user_service.mapper.UserMapper;
import com.orderapp.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }
        User user = userMapper.toEntity(request);   // DTO → Entity
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);         // Entity → Response DTO
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return userMapper.toResponse(user);
    }
}
