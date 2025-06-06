package com.topographe.topographe.controller;

import com.topographe.topographe.dto.request.AdminCreateRequest;
import com.topographe.topographe.dto.response.AdminResponse;
import com.topographe.topographe.dto.response.ApiResponse;
import com.topographe.topographe.dto.response.UserPageResponse;
import com.topographe.topographe.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        AdminResponse adminResponse = adminService.createAdmin(request);
        ApiResponse<AdminResponse> response = new ApiResponse<>(
                "Admin créé avec succès",
                adminResponse,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<UserPageResponse<AdminResponse>>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        UserPageResponse<AdminResponse> pageResponse = adminService.getAllAdmins(page, size, sortBy, sortDir);
        ApiResponse<UserPageResponse<AdminResponse>> response = new ApiResponse<>(
                "Liste des admins récupérée avec succès",
                pageResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(@PathVariable Long id) {
        AdminResponse adminResponse = adminService.getAdminById(id);
        ApiResponse<AdminResponse> response = new ApiResponse<>(
                "Admin trouvé avec succès",
                adminResponse,
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}