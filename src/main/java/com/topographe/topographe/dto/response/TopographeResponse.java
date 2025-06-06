package com.topographe.topographe.dto.response;

import com.topographe.topographe.entity.enumm.Role;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TopographeResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String cin;
    private String cityName;
    private Role role;
    private String licenseNumber;
    private String specialization;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private int totalClients;
    private int totalTechniciens;
    private int totalProjects;
}