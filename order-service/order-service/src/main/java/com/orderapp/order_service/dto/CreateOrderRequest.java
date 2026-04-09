package com.orderapp.order_service.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateOrderRequest(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Product name is required")
        String productName,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotNull(message = "Total price is required")
        BigDecimal totalPrice

) {}
