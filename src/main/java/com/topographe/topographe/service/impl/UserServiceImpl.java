package com.topographe.topographe.service.impl;

import com.topographe.topographe.dto.UserDTO;
import com.topographe.topographe.repository.UserRepository;
import com.topographe.topographe.service.UserService;
import com.topographe.topographe.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id).map(UserMapper::toDTO).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(UserMapper::toDTO).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toDTO);
    }
} 