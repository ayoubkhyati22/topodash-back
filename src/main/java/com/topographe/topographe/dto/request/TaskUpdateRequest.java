package com.topographe.topographe.dto.request;

import com.topographe.topographe.entity.enumm.TaskStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class TaskUpdateRequest {
    private String title;
    private String description;
    private Set<Long> assignedTechnicienIds; // Changé pour supporter plusieurs techniciens
    private LocalDate dueDate;
    private TaskStatus status;
    private Integer progressPercentage;
    private String progressNotes;
}