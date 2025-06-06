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

@Component
public class TaskMapper {

    public Task toEntity(TaskCreateRequest request, Project project, Technicien assignedTechnicien) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setAssignedTechnicien(assignedTechnicien);
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
        return task;
    }

    public void updateEntity(Task task, TaskUpdateRequest request, Technicien assignedTechnicien) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignedTechnicien(assignedTechnicien);
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
    }

    public TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());

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

        // Informations du technicien assigné
        if (task.getAssignedTechnicien() != null) {
            Technicien technicien = task.getAssignedTechnicien();
            response.setAssignedTechnicienId(technicien.getId());
            response.setAssignedTechnicienName(technicien.getFirstName() + " " + technicien.getLastName());
            response.setAssignedTechnicienSkillLevel(technicien.getSkillLevel().name());
            response.setAssignedTechnicienSpecialties(technicien.getSpecialties());
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
            response.setIsOverdue(daysRemaining < 0 && task.getStatus() != com.topographe.topographe.entity.enumm.TaskStatus.COMPLETED);
            response.setIsDueSoon(daysRemaining >= 0 && daysRemaining <= 3 && task.getStatus() != com.topographe.topographe.entity.enumm.TaskStatus.COMPLETED);

            // Calcul de la priorité basée sur la date d'échéance
            if (task.getStatus() == com.topographe.topographe.entity.enumm.TaskStatus.COMPLETED) {
                response.setPriority("COMPLETED");
            } else if (daysRemaining < 0) {
                response.setPriority("CRITICAL");
            } else if (daysRemaining <= 1) {
                response.setPriority("HIGH");
            } else if (daysRemaining <= 7) {
                response.setPriority("MEDIUM");
            } else {
                response.setPriority("LOW");
            }
        } else {
            response.setDaysRemaining(null);
            response.setIsOverdue(false);
            response.setIsDueSoon(false);
            response.setPriority(task.getStatus() == com.topographe.topographe.entity.enumm.TaskStatus.COMPLETED ? "COMPLETED" : "LOW");
        }

        return response;
    }
}