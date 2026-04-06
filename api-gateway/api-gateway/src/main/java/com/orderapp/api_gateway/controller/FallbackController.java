package com.orderapp.api_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/users")
    public Map<String, String> usersFallback() {
        return Map.of(
                "status",  "error",
                "message", "User service is temporarily unavailable. Please try again."
        );
    }
}
