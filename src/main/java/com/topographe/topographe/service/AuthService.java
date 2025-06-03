package com.topographe.topographe.service;

import com.topographe.topographe.dto.AuthResponse;
import com.topographe.topographe.dto.LoginRequest;
import com.topographe.topographe.dto.RegisterRequest;
import com.topographe.topographe.entity.User;

import java.util.List;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByUsername(String username);
}