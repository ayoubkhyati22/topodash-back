package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.UserDTO;
import com.topographe.topographe.entity.User;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRole()
        );
    }
} 