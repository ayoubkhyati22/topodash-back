package com.topographe.topographe.dto.request;

import com.topographe.topographe.entity.enumm.SkillLevel;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TechnicienUpdateRequest {

    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private Long cityId;
    private SkillLevel skillLevel;
    private String specialties;
    private Long assignedToTopographeId;
}