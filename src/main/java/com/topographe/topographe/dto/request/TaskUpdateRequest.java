package com.topographe.topographe.dto.request;

import com.topographe.topographe.entity.enumm.TaskStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskUpdateRequest {

    private String title;
    private String description;
    private Long assignedTechnicienId;
    private LocalDate dueDate;
    private TaskStatus status;
}