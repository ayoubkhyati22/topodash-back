package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.request.AdminCreateRequest;
import com.topographe.topographe.dto.response.AdminResponse;
import com.topographe.topographe.entity.Admin;
import com.topographe.topographe.entity.enumm.Role;
import com.topographe.topographe.entity.referentiel.City;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public Admin toEntity(AdminCreateRequest request, City city, String encodedPassword) {
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(encodedPassword);
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setBirthday(request.getBirthday());
        admin.setCin(request.getCin());
        admin.setCity(city);
        admin.setRole(Role.ADMIN);
        admin.setAdminLevel(request.getAdminLevel());
        admin.setIsActive(true);
        return admin;
    }

    public AdminResponse toResponse(Admin admin) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setEmail(admin.getEmail());
        response.setPhoneNumber(admin.getPhoneNumber());
        response.setFirstName(admin.getFirstName());
        response.setLastName(admin.getLastName());
        response.setBirthday(admin.getBirthday());
        response.setCin(admin.getCin());
        response.setCityName(admin.getCity().getName());
        response.setRole(admin.getRole());
        response.setAdminLevel(admin.getAdminLevel());
        response.setCreatedAt(admin.getCreatedAt());
        response.setIsActive(admin.getIsActive());
        return response;
    }
}