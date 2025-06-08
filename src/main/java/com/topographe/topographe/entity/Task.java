package com.topographe.topographe.entity;

import com.topographe.topographe.entity.enumm.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Changement : Relation Many-to-Many avec les techniciens
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_technicien",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "technicien_id")
    )
    private Set<Technicien> assignedTechniciens = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Pourcentage de progression (0-100)
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    // Notes sur l'avancement
    @Column(name = "progress_notes", length = 1000)
    private String progressNotes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (progressPercentage == null) {
            progressPercentage = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == TaskStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
            progressPercentage = 100;
        }
    }

    // Méthodes utilitaires
    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !isCompleted();
    }

    public void addTechnicien(Technicien technicien) {
        this.assignedTechniciens.add(technicien);
        technicien.getTasks().add(this);
    }

    public void removeTechnicien(Technicien technicien) {
        this.assignedTechniciens.remove(technicien);
        technicien.getTasks().remove(this);
    }
}