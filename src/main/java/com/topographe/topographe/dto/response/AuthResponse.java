package com.topographe.topographe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String email;
    private String role;
    private String phoneNumber;
    private String token;
}