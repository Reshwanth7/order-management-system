package com.orderapp.user_service.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {}
// Notice: no passwordHash — it never leaves the server