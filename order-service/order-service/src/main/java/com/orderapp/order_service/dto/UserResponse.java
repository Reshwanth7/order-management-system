package com.orderapp.order_service.dto;

// UserResponse.java  (mirrors user-service's response — used by Feign)
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {}
