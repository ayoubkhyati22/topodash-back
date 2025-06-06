package com.topographe.topographe.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TopographeCreateRequest {

    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String cin;
    private Long cityId;
    private String licenseNumber;
    private String specialization;
}