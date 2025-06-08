package com.topographe.topographe.dto.request;

import com.topographe.topographe.entity.enumm.ClientType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClientCreateRequest {

    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String cin;
    private Long cityId;
    private ClientType clientType;
    private String companyName;

    private Long createdByTopographeId;
}