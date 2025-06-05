package com.topographe.topographe.service;

import com.topographe.topographe.dto.response.AuthResponse;
import com.topographe.topographe.dto.request.LoginRequest;
import com.topographe.topographe.dto.request.RegisterRequest;
import com.topographe.topographe.entity.User;

import java.util.List;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByUsername(String username);
}