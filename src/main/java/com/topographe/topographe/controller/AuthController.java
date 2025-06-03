package com.topographe.topographe.controller;

import com.topographe.topographe.dto.LoginRequest;
import com.topographe.topographe.dto.RegisterRequest;
import com.topographe.topographe.service.AuthService;
import com.topographe.topographe.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody RegisterRequest request) {
        var result = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>("Registration successful", result, HttpStatus.OK.value()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        var result = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>("Login successful", result, HttpStatus.OK.value()));
    }
}



