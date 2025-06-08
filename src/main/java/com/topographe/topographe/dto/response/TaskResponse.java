package com.topographe.topographe.dto.response;

import com.topographe.topographe.entity.enumm.TaskStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

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

    // Informations des techniciens assignés (changé pour supporter plusieurs)
    private List<TechnicienInfo> assignedTechniciens;
    private int assignedTechniciensCount;
    private String assignedTechniciensNames; // Noms concaténés

    // Informations de progression
    private Integer progressPercentage;
    private String progressNotes;

    // Informations temporelles
    private Integer daysRemaining;
    private Boolean isOverdue;
    private Boolean isDueSoon; // Dans les 3 prochains jours
    private Integer daysSinceCreation;
    private Integer daysToComplete; // Durée pour terminer la tâche

    // Priorité calculée basée sur la date d'échéance
    private String priority; // HIGH, MEDIUM, LOW, CRITICAL

    // Classe interne pour les informations des techniciens
    @Data
    public static class TechnicienInfo {
        private Long id;
        private String name;
        private String skillLevel;
        private String specialties;
        private Boolean isActive; // Corrigé : active au lieu de isActive pour la cohérence
    }
}