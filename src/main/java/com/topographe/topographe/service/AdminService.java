package com.topographe.topographe.service;

import com.topographe.topographe.dto.request.AdminCreateRequest;
import com.topographe.topographe.dto.response.AdminResponse;
import com.topographe.topographe.dto.response.PageResponse;

public interface AdminService {
    AdminResponse createAdmin(AdminCreateRequest request);
    PageResponse<AdminResponse> getAllAdmins(int page, int size, String sortBy, String sortDir);
    AdminResponse getAdminById(Long id);
}