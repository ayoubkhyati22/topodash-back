package com.topographe.topographe.dto.request;

import com.topographe.topographe.entity.enumm.ProjectStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProjectUpdateRequest {

    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
}