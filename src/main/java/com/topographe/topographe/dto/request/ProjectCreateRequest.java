package com.topographe.topographe.dto.request;

import com.topographe.topographe.entity.enumm.ProjectStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProjectCreateRequest {

    private String name;
    private String description;
    private Long clientId;
    private Long topographeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status = ProjectStatus.PLANNING;
}