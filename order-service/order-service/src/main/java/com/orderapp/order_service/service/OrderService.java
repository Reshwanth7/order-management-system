package com.orderapp.order_service.service;

import com.orderapp.order_service.client.UserServiceClient;
import com.orderapp.order_service.dto.CreateOrderRequest;
import com.orderapp.order_service.dto.OrderResponse;
import com.orderapp.order_service.entity.Order;
import com.orderapp.order_service.exception.UserNotFoundException;
import com.orderapp.order_service.exception.OrderNotFoundException;
import com.orderapp.order_service.repository.OrderRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;

    public OrderService(OrderRepository orderRepository,
                        UserServiceClient userServiceClient) {
        this.orderRepository = orderRepository;
        this.userServiceClient = userServiceClient;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "createOrderFallback")
    @Retry(name = "userService")
    public OrderResponse createOrder(CreateOrderRequest request) {
        try {
            userServiceClient.getUserById(request.userId());
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException(request.userId());
        }

        Order order = Order.builder()
                .userId(request.userId())
                .productName(request.productName())
                .quantity(request.quantity())
                .totalPrice(request.totalPrice())
                .status("PENDING")
                .build();

        return toResponse(orderRepository.save(order));
    }

    // Fallback — called when circuit is open or all retries exhausted
    public OrderResponse createOrderFallback(CreateOrderRequest request,
                                             Exception ex) {
        // Return a queued/degraded response instead of failing completely
        return new OrderResponse(
                null,
                request.userId(),
                request.productName(),
                request.quantity(),
                request.totalPrice(),
                "QUEUED - user-service unavailable, will process when available",
                null
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
