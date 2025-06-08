package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.request.TaskCreateRequest;
import com.topographe.topographe.dto.request.TaskUpdateRequest;
import com.topographe.topographe.dto.response.TaskResponse;
import com.topographe.topographe.entity.Project;
import com.topographe.topographe.entity.Task;
import com.topographe.topographe.entity.Technicien;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public Task toEntity(TaskCreateRequest request, Project project, Set<Technicien> assignedTechniciens) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setAssignedTechniciens(assignedTechniciens);
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
        task.setProgressPercentage(request.getProgressPercentage());
        task.setProgressNotes(request.getProgressNotes());
        return task;
    }

    public void updateEntity(Task task, TaskUpdateRequest request, Set<Technicien> assignedTechniciens) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedTechniciens(assignedTechniciens);
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
        task.setProgressPercentage(request.getProgressPercentage());
        task.setProgressNotes(request.getProgressNotes());
    }

    public TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setCompletedAt(task.getCompletedAt());
        response.setProgressPercentage(task.getProgressPercentage());
        response.setProgressNotes(task.getProgressNotes());

        // Informations du projet
        Project project = task.getProject();
        response.setProjectId(project.getId());
        response.setProjectName(project.getName());
        response.setProjectStatus(project.getStatus().name());

        // Informations du client (via projet)
        response.setClientId(project.getClient().getId());
        response.setClientName(project.getClient().getFirstName() + " " + project.getClient().getLastName());
        response.setClientType(project.getClient().getClientType().name());

        // Informations du topographe (via projet)
        response.setTopographeId(project.getTopographe().getId());
        response.setTopographeName(project.getTopographe().getFirstName() + " " + project.getTopographe().getLastName());

        // Informations des techniciens assignés
        if (task.getAssignedTechniciens() != null && !task.getAssignedTechniciens().isEmpty()) {
            List<TaskResponse.TechnicienInfo> technicienInfos = task.getAssignedTechniciens().stream()
                    .map(technicien -> {
                        TaskResponse.TechnicienInfo info = new TaskResponse.TechnicienInfo();
                        info.setId(technicien.getId());
                        info.setName(technicien.getFirstName() + " " + technicien.getLastName());
                        info.setSkillLevel(technicien.getSkillLevel().name());
                        info.setSpecialties(technicien.getSpecialties());
                        info.setIsActive(technicien.getIsActive());
                        return info;
                    })
                    .collect(Collectors.toList());

            response.setAssignedTechniciens(technicienInfos);
            response.setAssignedTechniciensCount(technicienInfos.size());
            response.setAssignedTechniciensNames(
                    technicienInfos.stream()
                            .map(TaskResponse.TechnicienInfo::getName)
                            .collect(Collectors.joining(", "))
            );
        } else {
            response.setAssignedTechniciensCount(0);
            response.setAssignedTechniciensNames("");
        }

        // Calculs temporels
        LocalDate today = LocalDate.now();

        // Jours depuis la création
        long daysSinceCreation = ChronoUnit.DAYS.between(task.getCreatedAt().toLocalDate(), today);
        response.setDaysSinceCreation((int) daysSinceCreation);

        // Gestion de la date d'échéance
        if (task.getDueDate() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(today, task.getDueDate());
            response.setDaysRemaining((int) daysRemaining);
            response.setIsOverdue(task.isOverdue());
            response.setIsDueSoon(daysRemaining >= 0 && daysRemaining <= 3 && !task.isCompleted());

            // Calcul de la priorité basée sur la date d'échéance et le statut
            response.setPriority(calculatePriority(task, daysRemaining));
        } else {
            response.setDaysRemaining(null);
            response.setIsOverdue(false);
            response.setIsDueSoon(false);
            response.setPriority(task.isCompleted() ? "COMPLETED" : "LOW");
        }

        // Calcul du temps pour compléter (si complétée)
        if (task.getCompletedAt() != null) {
            long daysToComplete = ChronoUnit.DAYS.between(task.getCreatedAt().toLocalDate(),
                    task.getCompletedAt().toLocalDate());
            response.setDaysToComplete((int) daysToComplete);
        }

        return response;
    }

    private String calculatePriority(Task task, long daysRemaining) {
        if (task.isCompleted()) {
            return "COMPLETED";
        }

        if (task.isOverdue()) {
            return "CRITICAL";
        }

        if (daysRemaining <= 0) {
            return "CRITICAL";
        } else if (daysRemaining <= 1) {
            return "HIGH";
        } else if (daysRemaining <= 3) {
            return "MEDIUM";
        } else if (daysRemaining <= 7) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}