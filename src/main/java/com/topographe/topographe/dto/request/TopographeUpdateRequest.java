package com.topographe.topographe.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TopographeUpdateRequest {

    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Long cityId;
    private String specialization;
}