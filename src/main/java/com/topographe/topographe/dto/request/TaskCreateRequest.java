// TaskCreateRequest.java
package com.topographe.topographe.dto.request;

import com.topographe.topographe.entity.enumm.TaskStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class TaskCreateRequest {
    private String title;
    private String description;
    private Long projectId;
    private Set<Long> assignedTechnicienIds; // Changé pour supporter plusieurs techniciens
    private LocalDate dueDate;
    private TaskStatus status = TaskStatus.TODO;
    private Integer progressPercentage = 0;
    private String progressNotes;
}