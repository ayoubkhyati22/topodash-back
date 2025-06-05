package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.response.AuthResponse;
import com.topographe.topographe.dto.request.LoginRequest;
import com.topographe.topographe.dto.request.RegisterRequest;
import com.topographe.topographe.entity.Role;
import com.topographe.topographe.entity.User;
import com.topographe.topographe.repository.UserRepository;
import com.topographe.topographe.security.JwtUtil;
import com.topographe.topographe.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Username or email already in use");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));
        user.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(user.getUsername(), user.getEmail(), user.getRole().toString(), user.getPhoneNumber(), token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(user.getUsername(), user.getEmail(), user.getRole().toString(), user.getPhoneNumber(),  token);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }
}