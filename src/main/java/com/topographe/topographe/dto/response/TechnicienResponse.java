package com.topographe.topographe.dto.response;

import com.topographe.topographe.entity.enumm.Role;
import com.topographe.topographe.entity.enumm.SkillLevel;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TechnicienResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String cin;
    private String cityName;
    private Role role;
    private SkillLevel skillLevel;
    private String specialties;
    private String assignedToTopographeName;
    private Long assignedToTopographeId;
    private LocalDateTime createdAt;
    private Boolean isActive;

    // Statistiques des tâches
    private int totalTasks;
    private int activeTasks;
    private int completedTasks;
    private int todoTasks;
    private int reviewTasks;

    // Nouvelles statistiques des projets
    private int totalProjects;
    private int activeProjects;
    private int completedProjects;

    // Indicateurs de performance
    private double workloadPercentage;  // Pourcentage de charge de travail
    private boolean available;          // Corrigé : available au lieu de isAvailable
    private int maxRecommendedTasks;   // Nombre max recommandé de tâches actives

    // Statistiques de performance
    private double completionRate;     // Taux de completion des tâches (%)
    private double averageTasksPerProject; // Moyenne de tâches par projet
}