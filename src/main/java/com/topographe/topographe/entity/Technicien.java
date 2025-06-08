package com.topographe.topographe.entity;

import com.topographe.topographe.entity.enumm.SkillLevel;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@DiscriminatorValue("TECHNICIEN")
public class Technicien extends User {

    @Column(name = "skill_level")
    @Enumerated(EnumType.STRING)
    private SkillLevel skillLevel;

    @Column(name = "specialties")
    private String specialties;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_topographe_id", nullable = false)
    private Topographe assignedTo;

    // Changement : Relation Many-to-Many avec les tâches
    @ManyToMany(mappedBy = "assignedTechniciens", fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    // Méthodes utilitaires pour calculer les statistiques
    public Set<Project> getProjects() {
        return tasks.stream()
                .map(Task::getProject)
                .collect(Collectors.toSet());
    }

    public long getTotalTasksCount() {
        return tasks.size();
    }

    public long getActiveTasksCount() {
        return tasks.stream()
                .filter(task -> task.getStatus() == com.topographe.topographe.entity.enumm.TaskStatus.IN_PROGRESS)
                .count();
    }

    public long getCompletedTasksCount() {
        return tasks.stream()
                .filter(task -> task.getStatus() == com.topographe.topographe.entity.enumm.TaskStatus.COMPLETED)
                .count();
    }

    public long getTodoTasksCount() {
        return tasks.stream()
                .filter(task -> task.getStatus() == com.topographe.topographe.entity.enumm.TaskStatus.TODO)
                .count();
    }

    public long getReviewTasksCount() {
        return tasks.stream()
                .filter(task -> task.getStatus() == com.topographe.topographe.entity.enumm.TaskStatus.REVIEW)
                .count();
    }

    public long getTotalProjectsCount() {
        return getProjects().size();
    }

    public long getActiveProjectsCount() {
        return getProjects().stream()
                .filter(project -> project.getStatus() == com.topographe.topographe.entity.enumm.ProjectStatus.IN_PROGRESS)
                .count();
    }

    public long getCompletedProjectsCount() {
        return getProjects().stream()
                .filter(project -> project.getStatus() == com.topographe.topographe.entity.enumm.ProjectStatus.COMPLETED)
                .count();
    }

    // Calcul du taux de charge (nombre de tâches actives)
    public double getWorkloadPercentage() {
        long activeTasks = getActiveTasksCount();
        // Considérons 5 tâches actives comme 100% de charge
        return Math.min((activeTasks / 5.0) * 100, 100);
    }

    // Vérifier si le technicien est disponible pour de nouvelles tâches
    public boolean isAvailable(int maxActiveTasks) {
        return getActiveTasksCount() < maxActiveTasks;
    }
}