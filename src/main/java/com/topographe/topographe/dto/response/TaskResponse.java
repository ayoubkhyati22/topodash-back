package com.topographe.topographe.dto.response;

import com.topographe.topographe.entity.enumm.TaskStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;

    // Informations du projet
    private Long projectId;
    private String projectName;
    private String projectStatus;

    // Informations du client (via projet)
    private Long clientId;
    private String clientName;
    private String clientType;

    // Informations du topographe (via projet)
    private Long topographeId;
    private String topographeName;

    // Informations du technicien assigné
    private Long assignedTechnicienId;
    private String assignedTechnicienName;
    private String assignedTechnicienSkillLevel;
    private String assignedTechnicienSpecialties;

    // Informations temporelles
    private Integer daysRemaining;
    private Boolean isOverdue;
    private Boolean isDueSoon; // Dans les 3 prochains jours
    private Integer daysSinceCreation;

    // Priorité calculée basée sur la date d'échéance
    private String priority; // HIGH, MEDIUM, LOW
}