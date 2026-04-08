package com.orderapp.user_service.service;

import com.orderapp.user_service.dto.CreateUserRequest;
import com.orderapp.user_service.dto.UserResponse;
import com.orderapp.user_service.entity.User;
import com.orderapp.user_service.exception.EmailAlreadyExistsException;
import com.orderapp.user_service.exception.UserNotFoundException;
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

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email()); // was IllegalArgumentException
        }
        User user = userMapper.toEntity(request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)); // was RuntimeException
        return userMapper.toResponse(user);
    }
}
