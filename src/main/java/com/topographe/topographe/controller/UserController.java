package com.topographe.topographe.controller;

import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.RefPageResponse;
import com.topographe.topographe.dto.UserDTO;
import com.topographe.topographe.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page/{page}/size/{size}")
    public ResponseEntity<ApiResponse<RefPageResponse<UserDTO>>> getAllUsers(
            @PathVariable int page,
            @PathVariable int size) {
        Page<UserDTO> userPage = userService.getAllUsers(PageRequest.of(page, size));
        RefPageResponse<UserDTO> response = new RefPageResponse<>(
            userPage.getContent(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.getNumber(),
            userPage.getSize()
        );
        return ResponseEntity.ok(new ApiResponse<>("All users fetched", response, HttpStatus.OK.value()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>("User fetched", user, HttpStatus.OK.value()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(new ApiResponse<>("User fetched", user, HttpStatus.OK.value()));
    }
} 