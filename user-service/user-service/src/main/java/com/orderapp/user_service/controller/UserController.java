package com.orderapp.user_service.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from User Service on port 9091!";
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable("id") Long id) {
        return Map.of(
                "id",    id,
                "name",  "Test User",
                "email", "test@example.com",
                "port",  "9091"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "user-service");
    }
}
