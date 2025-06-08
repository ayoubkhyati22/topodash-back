package com.topographe.topographe.entity;

import com.topographe.topographe.entity.enumm.ProjectStatus;
import com.topographe.topographe.entity.enumm.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topographe_id", nullable = false)
    private Topographe topographe;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Méthodes de calcul des statistiques

    public int getTotalTasksCount() {
        return tasks.size();
    }

    public long getTodoTasksCount() {
        return tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.TODO)
                .count();
    }

    public long getInProgressTasksCount() {
        return tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
    }

    public long getReviewTasksCount() {
        return tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.REVIEW)
                .count();
    }

    public long getCompletedTasksCount() {
        return tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
    }

    /**
     * Calcule le pourcentage de progression basé sur les tâches terminées
     * @return pourcentage de 0 à 100
     */
    public double getProgressPercentage() {
        if (tasks.isEmpty()) {
            return 0.0;
        }

        long completedTasks = getCompletedTasksCount();
        double percentage = ((double) completedTasks / tasks.size()) * 100;
        return Math.round(percentage * 100.0) / 100.0; // Arrondi à 2 décimales
    }

    /**
     * Calcule le pourcentage de progression pondéré basé sur les statuts des tâches
     * TODO = 0%, IN_PROGRESS = 50%, REVIEW = 80%, COMPLETED = 100%
     * @return pourcentage pondéré de 0 à 100
     */
    public double getWeightedProgressPercentage() {
        if (tasks.isEmpty()) {
            return 0.0;
        }

        double totalWeight = 0.0;
        for (Task task : tasks) {
            switch (task.getStatus()) {
                case TODO -> totalWeight += 0.0;
                case IN_PROGRESS -> totalWeight += 50.0;
                case REVIEW -> totalWeight += 80.0;
                case COMPLETED -> totalWeight += 100.0;
            }
        }

        double percentage = totalWeight / tasks.size();
        return Math.round(percentage * 100.0) / 100.0;
    }

    /**
     * Vérifie si le projet est en retard
     * @return true si le projet est en retard
     */
    public boolean isOverdue() {
        return endDate != null &&
                endDate.isBefore(LocalDate.now()) &&
                status != ProjectStatus.COMPLETED &&
                status != ProjectStatus.CANCELLED;
    }

    /**
     * Calcule les jours restants jusqu'à la date de fin
     * @return nombre de jours restants (négatif si en retard)
     */
    public Long getDaysRemaining() {
        if (endDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    /**
     * Vérifie si le projet est terminé
     * @return true si le projet est terminé
     */
    public boolean isCompleted() {
        return status == ProjectStatus.COMPLETED;
    }

    /**
     * Obtient tous les techniciens assignés au projet
     * @return Set de techniciens uniques
     */
    public Set<Technicien> getAssignedTechniciens() {
        Set<Technicien> techniciens = new HashSet<>();
        for (Task task : tasks) {
            techniciens.addAll(task.getAssignedTechniciens());
        }
        return techniciens;
    }

    /**
     * Compte le nombre de techniciens assignés au projet
     * @return nombre de techniciens uniques
     */
    public int getAssignedTechniciensCount() {
        return getAssignedTechniciens().size();
    }
}