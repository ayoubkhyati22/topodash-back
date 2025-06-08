package com.topographe.topographe.dto.response;

import com.topographe.topographe.entity.enumm.ProjectStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    // Informations du client
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientType;
    private String clientCompanyName;

    // Informations du topographe
    private Long topographeId;
    private String topographeName;
    private String topographeEmail;
    private String topographeLicenseNumber;

    // Statistiques des tâches
    private int totalTasks;
    private int todoTasks;
    private int inProgressTasks;
    private int reviewTasks;
    private int completedTasks;

    // Informations de progression améliorées
    private Double progressPercentage;          // Basé sur tâches terminées
    private Double weightedProgressPercentage;  // Pondéré selon statuts
    private Integer daysRemaining;
    private Boolean isOverdue;
    private Boolean isCompleted;

    // Nouvelles informations sur les techniciens
    private int assignedTechniciensCount;
    private String assignedTechniciensNames;    // Noms des techniciens assignés

    // Indicateurs de santé du projet
    private String healthStatus;  // GOOD, WARNING, CRITICAL
    private String healthMessage; // Message explicatif

    // Statistiques temporelles
    private Integer totalDuration;     // Durée totale en jours
    private Integer elapsedDuration;   // Jours écoulés depuis le début
    private Double timeProgressPercentage; // Progression temporelle
}