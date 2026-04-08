package com.orderapp.user_service.service;


import com.orderapp.user_service.dto.AuthResponse;
import com.orderapp.user_service.dto.CreateUserRequest;
import com.orderapp.user_service.dto.LoginRequest;
import com.orderapp.user_service.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtService jwtService,
                       AuthenticationManager authManager, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(CreateUserRequest request) {
        // Hash the password before storing
        CreateUserRequest hashed = new CreateUserRequest(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        userService.createUser(hashed);
        String token = jwtService.generateToken(request.email());
        return new AuthResponse(token, request.email());
    }

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException if wrong — Spring handles it as 401
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = jwtService.generateToken(request.email());
        return new AuthResponse(token, request.email());
    }
}
