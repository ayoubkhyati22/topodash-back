package com.topographe.topographe.dto.request;

import com.topographe.topographe.entity.enumm.ClientType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClientUpdateRequest {

    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Long cityId;
    private ClientType clientType;
    private String companyName;
}