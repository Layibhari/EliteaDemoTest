package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
public class HealthCheckController {

    @GetMapping("/api/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.status(HttpStatus.OK)
            .body("{\"status\": \"UP\"}");
    }
}